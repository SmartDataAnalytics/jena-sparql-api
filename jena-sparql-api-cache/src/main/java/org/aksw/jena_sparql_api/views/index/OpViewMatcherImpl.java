package org.aksw.jena_sparql_api.views.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeImpl;
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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
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
	    Set<QueryIndex> tmpCands = new HashSet<>();

        itemFeatureExtractor.apply(normalizedItem).forEach(featureSet -> {
            //featuresToIndexes.getIfSubsetOf(featureSet).stream()
            featuresToIndexes.get(featureSet).stream()
                //.map(e -> e.getValue())
                .forEach(x -> tmpCands.add(x));
        });

        // Order candidates by their node count - largest node counts first
        List<QueryIndex> cands = new ArrayList<>(tmpCands);
        Collections.sort(cands, (a, b) -> ((int)(a.getTree().nodeCount() - b.getTree().nodeCount())));

        logger.debug("Phase 1: " + cands.size() + "/" + featuresToIndexes.size() + " passed");
        QueryIndex queryIndex = itemIndexer.apply(normalizedItem);


        for(QueryIndex cacheIndex : cands) {


            Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(cacheIndex, queryIndex);
            Tree<Op> cacheTree = cacheIndex.getTree();
            Tree<Op> queryTree = queryIndex.getTree();

            // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
            // In fact, we could use these features as an additional index
            Stream<OpVarMap> opVarMaps = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, cacheTree, queryTree);

            opVarMaps.forEach(opVarMap -> {
                Op cacheRoot = cacheTree.getRoot();
                Op queryRoot = opVarMap.getOpMapping().get(cacheRoot);
                System.out.println("query root: " + queryRoot);

                // We need to update the queryIndex (remove sub-trees that matched)
                Tree<Op> r = applyMapping(cacheTree, queryTree, opVarMap);

                System.out.println("Result: " + r);
            });
        }

		return null;
	}

	public static Tree<Op> applyMapping(Tree<Op> cacheTree, Tree<Op> queryTree, OpVarMap opVarMap) {
	    Map<Op, Op> nodeMapping = opVarMap.getOpMapping();

	    Op sourceRoot = cacheTree.getRoot();
	    Op targetNode = nodeMapping.get(sourceRoot);

	    if(targetNode == null) {
	        throw new RuntimeException("Could not match root node of a source tree to a node in the target tree - Should not happen.");
	    }

	    //QuadPattern yay = new QuadPattern();
	    Node serviceNode = NodeFactory.createURI("");
	    OpService placeholderOp = new OpService(serviceNode, new OpBGP(), true);
	    Op repl = OpUtils.substitute(queryTree.getRoot(), false, op -> {
	       return op == targetNode ? placeholderOp : null;
	    });

	    Tree<Op> result = OpUtils.createTree(repl);
	    return result;
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
