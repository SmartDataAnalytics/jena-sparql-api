package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinToConjunction;
import org.aksw.jena_sparql_api.algebra.transform.TransformUnionToDisjunction;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMap;
import org.aksw.jena_sparql_api.concept_cache.collection.FeatureMapImpl;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.unsorted.OpVisitorFeatureExtractor;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public class OpViewMatcherImpl
	implements OpViewMatcher
{
    
    private static final Logger logger = LoggerFactory
            .getLogger(OpViewMatcherImpl.class);

    protected Function<Op, Op> opNormalizer;
    //protected Function<Op, Op> opDenormalizer;

    protected Function<Op, Stream<Set<String>>> itemFeatureExtractor;
    protected Function<Op, QueryIndex> itemIndexer;
    protected FeatureMap<String, QueryIndex> featuresToIndexes;
	
    
	public OpViewMatcherImpl(
	        Function<Op, Op> opNormalizer,
            Function<Op, Stream<Set<String>>> itemFeatureExtractor,
            Function<Op, QueryIndex> itemIndexer) {
        super();
        this.opNormalizer = opNormalizer;
        this.itemFeatureExtractor = itemFeatureExtractor;
        this.itemIndexer = itemIndexer;
        this.featuresToIndexes = new FeatureMapImpl<>(); //featuresToIndexes;
    }

    @Override
	public void add(Op item) {
        Op normalizedItem = opNormalizer.apply(item);
	    QueryIndex index = itemIndexer.apply(normalizedItem);
	    
        itemFeatureExtractor.apply(normalizedItem).forEach(featureSet -> {
            featuresToIndexes.put(featureSet, index); // new SimpleEntry<>(item, data)
        });
		
	}

	@Override
	public Collection<Entry<Op, OpVarMap>> lookup(Op item) {
	    Op normalizedItem = opNormalizer.apply(item); 
	    Set<QueryIndex> cands = new HashSet<>();
	    
        itemFeatureExtractor.apply(normalizedItem).forEach(featureSet -> {
            //featuresToIndexes.getIfSubsetOf(featureSet).stream()
            featuresToIndexes.get(featureSet).stream()
                //.map(e -> e.getValue())
                .forEach(x -> cands.add(x));
        });
	    
        logger.debug("Phase 1: " + cands.size() + "/" + featuresToIndexes.size() + " passed");
        
        //
        QueryIndex queryIndex = itemIndexer.apply(normalizedItem);
	    
        cands.stream().map(cacheIndex -> {
            Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(cacheIndex, queryIndex);
            Tree<Op> cacheTree = cacheIndex.getTree();
            Tree<Op> queryTree = queryIndex.getTree();
            
            // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
            // In fact, we could use these features as an additional index
            Stream<OpVarMap> opVarMapping = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, cacheTree, queryTree);
            opVarMapping.forEach(x -> System.out.println("GOT: " + x));
            return null;
        }).count();
        
		return null;
	}

	public static Op normalizeOp(Op op) {
        op = Transformer.transform(TransformJoinToConjunction.fn, Transformer.transform(TransformUnionToDisjunction.fn, op)); 
        
        Generator<Var> generatorCache = VarGeneratorImpl2.create(); 
        Op result = OpUtils.substitute(op, false, (o) -> SparqlCacheUtils.tryCreateCqfp(o, generatorCache));
        
        return result;

	}

	
	public static OpViewMatcher create() {
        Function<Op, Stream<Set<String>>> itemFeatureExtractor = (oop) -> Collections.singleton(OpVisitorFeatureExtractor.getFeatures(oop, (op) -> op.getClass().getSimpleName())).stream();

        OpViewMatcher result = new OpViewMatcherImpl(
        		OpViewMatcherImpl::normalizeOp,        		
                itemFeatureExtractor,
                new QueryIndexerImpl());

        return result;
	}
}
