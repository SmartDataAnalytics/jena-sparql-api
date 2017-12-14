package org.aksw.jena_sparql_api.views.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.FeatureMapImpl;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.jena_sparql_api.algebra.transform.TransformDisjunctionToUnion;
import org.aksw.jena_sparql_api.algebra.transform.TransformEffectiveOp;
import org.aksw.jena_sparql_api.algebra.transform.TransformPushFiltersIntoBGP;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;


public class SparqlViewMatcherOpImpl<P>
    implements SparqlViewMatcherOp<P>
{

    private static final Logger logger = LoggerFactory
            .getLogger(SparqlViewMatcherOpImpl.class);

    //protected Function<Op, Op> opNormalizer;
    protected Rewrite opNormalizer;


    protected Function<Op, Set<Set<String>>> itemFeatureExtractor;
    protected Function<Op, OpIndex> itemIndexer;


    protected FeatureMap<String, P> featuresToIndexes;
    protected Map<P, OpIndex> idToQueryIndex;

    //protected Map<K, ProjectedOp> keyToValue;



    //int nextPatternId = 0;
    //P nextPatternId;
    Supplier<P> nextPatternIdSupplier;


    public SparqlViewMatcherOpImpl(
            Rewrite opNormalizer,
            Function<Op, Set<Set<String>>> itemFeatureExtractor,
            Function<Op, OpIndex> itemIndexer,
            Supplier<P> nextPatternIdSupplier) {
        super();
        this.opNormalizer = opNormalizer;
        this.itemFeatureExtractor = itemFeatureExtractor;
        this.itemIndexer = itemIndexer;
        this.featuresToIndexes = new FeatureMapImpl<>(); //featuresToIndexes;

        this.nextPatternIdSupplier = nextPatternIdSupplier;

        idToQueryIndex = new HashMap<>();


        //this.qfpcIndex = new SparqlViewCacheImpl();
    }

    public P allocate(Op item) {
        P result = nextPatternIdSupplier.get();
        put(result, item);
        return result;
    }


    @Override
    public void put(P key, Op item) {

        // Check whether the submitted op is an extended conjunctive query,
        //i.e. is only comprised of distinct, projection, filter and quad pattern in that order, whereas presence is optional


//    	Op normalizedItem = opNormalizer.rewrite(item);
//
//    	Node id = NodeFactory.createURI("id://" + StringUtils.md5Hash("" + normalizedItem));
        OpIndex index = itemIndexer.apply(item);

        Set<Set<String>> featureSets = index.getFeatureSets(); //itemFeatureExtractor.apply(item);
        //MyEntry<K> entry = new MyEntry<>(key, featureSets, index);

        for(Set<String> featureSet : featureSets) {
            featuresToIndexes.put(featureSet, key); // new SimpleEntry<>(item, data)
        }

        idToQueryIndex.put(key, index);

    }


    /**
     * Lookup a single candidate
     */
//    public LookupResult<K> lookupSingle(Op item) {
//        Collection<LookupResult<K>> tmp = lookup(item);
//        LookupResult<K> result = Iterables.getFirst(tmp, null);
//        return result;
//    }


    /**
     * Find matches among the extended conjunctive queries
     *
     *
     * @param item
     * @return
     */
    //@Override
//    public Collection<LookupResult> lookupSimpleMatches(Op item) {
//
//        return null;
//
//    }


//    public Stream<LookupResult<K>> filterByProjection(Stream<LookupResult<K>> lrs) {
//    	SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, vm)
//
//
//    	//Collection<LookupResult<K>>
//    }




    /**
     * The result is a list of candidate matches ordered by sepecificity.
     * The each item comprises the attributes:
     * - The entry-key
     * - The Op-Map
     * - The Var-Map
     * - The projected op
     * - (Optionally internal properties: OpIndex and FeatureSet)
     *
     * So we have two options how to structure the result:
     * Nested:
     *   From the op-mapping, navigate to the var-mapping and from there to the entry-data
     *   Map<Op, Op> -> Map<Var, Var> -> (Key, ProjectedOp)
     *
     * Flat:
     *   (key, opmap, varmap, projected op, [op index, feature set])
     *
     * Indirect:
     *   The result is:
     *   OpVarMap and patternId
     *
     *
     * The advantage of the flat structure is obviously the simplicity
     * The disadvantage of the flat structure is, that it may be suboptimal for efficient processing
     *
     *
     *
     *
     * @param pop
     * @return
     */
//     public Collection<KeyedOpVarMap<P>> lookup(ProjectedOp pop) {
//
//    	Op patternOp = pop.getResidualOp();
//    	Collection<KeyedOpVarMap<P>> candidates = lookup(patternOp);
//
//    	// For each pattern id, check the projection
//    	for(LookupResult<P> cand : candidates) {
//    		int patternId = cand.getEntry().id;
//
//			Map<K, ProjectedOp> keyToPop = patternIdToKeyToPop.getOrDefault(patternId, Collections.emptyMap());
//
//
//    		// TODO What if there are multiple opVarMaps?
//
//			// Note: For a given variable mapping, there can be multiple projections, such as{ (?s), (?s ?p), (?p) }
//			// So we first determine all compatible ones, and then sort them by specificity.
//			// We can then select one of the most specific ones.
//
//			// The lookup result is then: OpVarMap, with for every var map the list of projections
//			// ISSUE Using var maps as keys in a map feels wrong - but maybe in that case it is justified - as the set of candidate projections
//			// really depend on the variable mapping
//
//    		OpVarMap opVarMap = cand.getOpVarMap();
//
//    		// For each var map get the set of compatible projections
//    		Iterable<Map<Var,Var>> filteredVarMaps = () -> StreamUtils.stream(opVarMap.getVarMaps())
//    			.filter(varMap -> SparqlViewMatcherProjectionUtils.validateProjection(e.getValue().getProjection(), pop.getProjection(), varMap))
//    			.iterator();
//
//    		opVarMap = new OpVarMap(opVarMap.getOpMap(), filteredVarMaps);
//
//
//
//	    			// TODO Maybe make this lazy: Iterable<> foo = () -> keyToPop.stream()....interator()
//
//		    		List<Map<Var, Var>> varMaps = keyToPop.entrySet().stream()
//		    			.filter(e -> SparqlViewMatcherProjectionUtils.validateProjection(e.getValue().getProjection(), pop.getProjection(), varMap))
//		    			.collect(Collectors.toList());
//
//		    		OpVarMap r = new OpVarMap(opVarMap.getOpMap(), varMaps);
//		    		return r;
//    			})
//    			// Remove all candidates for which no projection was compatible
//    			.filter(arg0)
//
//
//    	}
//
//    	LookupResult<K> lr;
//    	lr.getEntry();
//
//
//
//    	return null;
//    }


    @Override
    public Map<P, OpVarMap> lookup(Op item) {
        //Op normalizedItem = opNormalizer.rewrite(item);
        Set<P> tmpCands = new HashSet<>();
//
//        itemFeatureExtractor.apply(item).forEach(featureSet -> {
//            //featuresToIndexes.getIfSubsetOf(featureSet).stream()
//            featuresToIndexes.get(featureSet).stream()
//                //.map(e -> e.getValue())
//                .forEach(x -> tmpCands.add(x));
//        });

        // TODO if there is a projection on item, the lookup fails, because
        // in the index we cut the projection away
        // So

        itemFeatureExtractor.apply(item).forEach(featureSet -> {
            //featuresToIndexes.getIfSubsetOf(featureSet).stream()
            featuresToIndexes.getIfSubsetOf(featureSet).stream()
                //.map(e -> e.getValue())
                .forEach(x -> tmpCands.add(x.getValue()));
        });


        // Order candidates by their node count - largest node counts (number of operators in the algebra tree) first
        List<P> cands = new ArrayList<>(tmpCands);
        Collections.sort(cands, (a, b) -> ((int)(idToQueryIndex.get(a).getTree().nodeCount() - idToQueryIndex.get(b).getTree().nodeCount())));

        if(logger.isDebugEnabled()) { logger.debug("Phase 1: " + cands.size() + "/" + featuresToIndexes.size() + " passed"); }
        OpIndex queryIndex = itemIndexer.apply(item);


        //List<KeyedOpVarMap<P>> result = new ArrayList<>();

        Map<P, OpVarMap> result = new LinkedHashMap<>();

        for(P cacheEntry : cands) {
            //OpIndex cacheIndex = cacheEntry.queryIndex;
            P id = cacheEntry;
            OpIndex cacheIndex = idToQueryIndex.get(cacheEntry);

            Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(cacheIndex, queryIndex);
            Tree<Op> cacheTree = cacheIndex.getTree();
            Tree<Op> queryTree = queryIndex.getTree();

            // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
            // In fact, we could use these features as an additional index
            Stream<OpVarMap> opVarMaps = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, cacheTree, queryTree);

            opVarMaps.forEach(opVarMap -> {
                Op cacheRoot = cacheTree.getRoot();
                Op queryRoot = opVarMap.getOpMap().get(cacheRoot);
                if(logger.isDebugEnabled()) { logger.debug("query root: " + queryRoot); }


                //K id = cacheEntry.id;
                // We need to update the queryIndex (remove sub-trees that matched)

                // This id is just for logging purposes
                Node tmpId = NodeFactory.createURI("http://tmpId-" + id);
                Tree<Op> r = applyMapping(tmpId, cacheTree, queryTree, opVarMap);

                if(logger.isDebugEnabled()) { logger.debug("Result: " + r); }
                if(logger.isDebugEnabled()) { logger.debug("Varmap: " + Iterables.toString(opVarMap.getVarMaps())); }

                //KeyedOpVarMap<P> lr = new KeyedOpVarMap<P>(cacheEntry, opVarMap);
                //result.add(lr);
                result.put(cacheEntry, opVarMap);
                //return lr;
            });

        }

        if(logger.isDebugEnabled()) {
            logger.debug("Final candidate list: " + result.size());
        }

        return result;
    }

    /**
     * Create a tree based on the queryTree where the subtree of the cacheTree has been replaced
     * with a OpService node which references the given id
     *
     *
     * @param id
     * @param cacheTree
     * @param queryTree
     * @param opVarMap
     * @return
     */
    public static <V> Tree<Op> applyMapping(Node id, Tree<Op> cacheTree, Tree<Op> queryTree, OpVarMap opVarMap) {
        Map<Op, Op> nodeMapping = opVarMap.getOpMap();

        Op sourceRoot = cacheTree.getRoot();
        Op targetNode = nodeMapping.get(sourceRoot);

        if(targetNode == null) {
            throw new RuntimeException("Could not match root node of a source tree to a node in the target tree - Should not happen.");
        }

        //QuadPattern yay = new QuadPattern();
        //Node serviceNode = NodeFactory.createURI("");
        OpService placeholderOp = new OpService(id, new OpBGP(), true);
        Op repl = OpUtils.substitute(queryTree.getRoot(), false, op -> {
           return op == targetNode ? placeholderOp : null;
        });

        Tree<Op> result = OpUtils.createTree(repl);
        return result;
    }



    public static Op queryToNormalizedOp(Query query) {
        Op result = Algebra.compile(query);
        result = Algebra.toQuadForm(result);
        result = QueryToGraph.normalizeOp(result, false);
        return result;
    }


    public static Op denormalizeOp(Op op) {
        // Replace QFPCs
        op = Transformer.transform(new TransformEffectiveOp(), op);

        //op = Transformer.transform(/new Transfo, op)
        op = TransformPushFiltersIntoBGP.transform(op);

        op = Transformer.transform(TransformDisjunctionToUnion.fn, op);

        return op;

        //op = Transformer.transform(TransformDisju, op);
        //op = Transformer.transform(TransformJoinToSequence.fn, op);
    }


    public static Set<Set<String>> extractFeatures(Op oop) {
        return Collections.singleton(OpVisitorFeatureExtractor.getFeatures(oop, (op) -> op.getClass().getSimpleName()));
    }

    public static SparqlViewMatcherOp<Integer> create() {
//        Function<Op, Set<Set<String>>> itemFeatureExtractor = (oop) ->
//            Collections.singleton(OpVisitorFeatureExtractor.getFeatures(oop, (op) -> op.getClass().getSimpleName()));

        Iterator<Integer> nextPatternIdIt =
                IntStream.generate(new AtomicInteger()::getAndIncrement).iterator();

        Supplier<Integer> supplier = () -> nextPatternIdIt.next();

        SparqlViewMatcherOp<Integer> result = new SparqlViewMatcherOpImpl<>(
                op -> QueryToGraph.normalizeOp(op, false),
                SparqlViewMatcherOpImpl::extractFeatures,
                new OpIndexerImpl(),
                supplier);

        return result;
    }

    @Override
    public void removeKey(Object key) {
        featuresToIndexes.removeValue(key);
        idToQueryIndex.remove(key);

//        MyEntry<K> e = idToQueryIndex.get(key);
//        if(e != null) {
//            Set<Set<String>> featureSets = e.featureSets;
//            featuresToIndexes.removeAll(featureSets);
//            idToQueryIndex.remove(key);
//        }
    }

    @Override
    public Op getPattern(P key) {
        OpIndex opIndex = idToQueryIndex.get(key);
        Op result = opIndex == null ? null : opIndex.getOp();
        return result;
    }
}
