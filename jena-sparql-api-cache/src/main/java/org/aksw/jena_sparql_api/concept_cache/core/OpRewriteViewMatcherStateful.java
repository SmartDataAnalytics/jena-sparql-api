package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.utils.ConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedOp;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcher;
import org.aksw.jena_sparql_api.concept_cache.dirty.ConjunctiveQueryMatcherImpl;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpcImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.util.collection.CacheRangeInfo;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherPop;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherPopImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;


/**
 * Rewrite an algebra expression under view matching
 *
 * Removes projection and distinct from the ops passed to the pattern matchers
 * in order to handle different projections on isomorphic patterns:
 * Given a view op, a query op and a solution OpVarMap. Based on the OpVarMaps root substitution node
 * in the query op, the query'ops mandatory variables at the matching root will be analyzed
 * In order for the OpVarMap to be a complete candidate, all mandatory variables must be mapped to.
 * I.e. map(projectedVars(viewPatternRoot), partialSolution) superseteq mandatoryVars(querySubstNode)
 *
 *
 *
 *
 * This is a stateful rewriter: It rewrites expressions by injecting references to auxiliary data,
 * and internally a map with further information about the auxiliary data is maintained.
 *
 * Probably the main point of the architecture is the unified handling of cache writes and reads using
 * RangeSuppliers:
 *
 *
 * @author raven
 *
 */
public class OpRewriteViewMatcherStateful
    implements RewriterSparqlViewMatcher
    //implements Rewrite
{

    private static final Logger logger = LoggerFactory.getLogger(OpRewriteViewMatcherStateful.class);

    // TODO Maybe bundle normalizer and denormalizer into one object
    //protected Rewrite opNormalizer;
    protected Rewrite opDenormalizer;

    protected SparqlViewMatcherPop<Node> viewMatcherTreeBased;
    //protected SparqlViewMatcherQfpc<Node> viewMatcherQuadPatternBased;
    protected ConjunctiveQueryMatcher<Node> viewMatcherQuadPatternBased;



    //protected Map<Node, Map<Node, VarInfo>> patternIdToStorageIdToVarInfo;
    //protected Map<Node, Node> storageIdToPatternId;

    // Maps result set ids to storage ids
    protected Cache<Node, StorageEntry> cache;
    //protected Map<Node, StorageEntry> storageMap = new HashMap<>();


    // Mapping from cache entry id to the cache data
    // - We need to be able to ask whether a certain range is completely in cache
    // - Ask which variables are defined by the cache data
    // -
    //protected Map<Node, ViewMatcherData> idToCacheData;


    public OpRewriteViewMatcherStateful(Cache<Node, StorageEntry> cache, Collection<RemovalListener<Node, StorageEntry>> removalListeners) {
        //this.opNormalizer = SparqlViewMatcherOpImpl::normalizeOp;
        this.opDenormalizer = SparqlViewMatcherOpImpl::denormalizeOp;
        this.viewMatcherTreeBased = SparqlViewMatcherPopImpl.create();
        this.viewMatcherQuadPatternBased = new ConjunctiveQueryMatcherImpl<>();
        this.cache = cache;


        //this.patternIdToStorageIdToVarInfo = new HashMap<>();
        //this.storageIdToPatternId = new HashMap<>();

        removalListeners.add((n)-> removeStorage(n.getKey()));
    }

    public void removeStorage(Node patternId) {
        //Node patternId = storageIdToPatternId.computeIfPresent(storageKey, (k, v) -> null);

        viewMatcherTreeBased.removeKey(patternId);
        viewMatcherQuadPatternBased.removeKey(patternId);

        // TODO Maybe there is potential to optimize with another computeIfPresent
        //Map<Node, VarInfo> map = patternIdToStorageIdToVarInfo.get(patternId);
//        map.remove(storageKey);
//        if(patternIdToStorageIdToVarInfo.isEmpty()) {
//            storageIdToPatternId.remove(storageKey);
//        }
    }


    public Cache<Node, StorageEntry> getCache() {
        return cache;
    }

    //@Override
    // TODO Do we need a further argument for the variable information?
    /**
     * The storageId is intended to associate result sets with it.
     *
     *
     * The patternId ignores the projection of the result set - i.e.
     *
     * The motivation is to reduce the number of candidate-tree mappings by
     * avoidmseparate entries for expressions that differ only in their projection.
     *
     * This approach is only half-assed, because if done properly, we would represent
     * all candidates in some trie-like structure that would efficiently allow matching
     * against all relevant candidates at once; and allow iteratively narrowing down the set of remaining
     * candidates based on the set of constituents (datalog rules?).
     *
     * And in my mind this triggers an association with generalized trie and thus rete algorithm
     * and thus rule systems / data log.
     * But maybe the my-algebra has even further advantages.
     *
     *
     *
     *
     * @param storageId
     * @param op
     */
    public void put(Node storageId, ProjectedOp pop) { // Op normalizedOp

        //ProjectedOp projectedOp = SparqlCacheUtils.cutProjectionAndNormalize(op, opNormalizer);
        //Op normalizedOp = projectedOp.getResidualOp();

        // TODO: Finish cutting away the projection
        // TODO Hack to map between ProjectedOp and VarInfo - make this consistent!
//        VarInfo varInfo;
//        if(projectedOp != null) {
//            varInfo = new VarInfo(new HashSet<>(projectedOp.getProjection().getVars()), 0);//Collections.emptySet());
//            normalizedOp = projectedOp.getResidualOp();
//        } else {
//            throw new RuntimeException("todo handle this case");
//            //OpVars.visibleVars(normalizedOp);
//        }

        // TODO Derive a proper patternId (maybe a hash from the normalizedOp?)
        Node patternId = storageId;

        // TODO Check if the pattern we are putting is isomorphic to an existing one
        //Node patternId = lookup(normalizedOp);

        //Map<Node, VarInfo> map = patternIdToStorageIdToVarInfo.computeIfAbsent(patternId,  (n) -> new HashMap<>());
        //map.put(storageId, varInfo);

        //storageIdToPatternId.put(storageId, patternId);


        // Allocate a new id entry of this op
        //Node result = NodeFactory.createURI("id://" + StringUtils.md5Hash("" + normalizedItem));

        // TODO Verify: Transform to qfpc directly (the normalizedOp has the projection cut away)
        //ProjectedQuadFilterPattern conjunctiveQuery = SparqlCacheUtils.transform(normalizedOp);
        //QuadFilterPattern qfp = SparqlCacheUtils.extractQuadFilterPattern(op);


        Op normalizedOp = pop.getResidualOp();

        OpExtConjunctiveQuery opQfpc = normalizedOp instanceof OpExtConjunctiveQuery
                ? (OpExtConjunctiveQuery)normalizedOp
                : null;

        if(opQfpc != null) {
            ConjunctiveQuery cq = opQfpc.getQfpc();//SparqlCacheUtils.canonicalize2(qfp, VarGeneratorImpl2.create());

            // TODO Make the quad based view matcher projection-aware
            viewMatcherQuadPatternBased.put(storageId, cq);
        }

        // Always add the whole op
        //else {
            viewMatcherTreeBased.put(storageId, pop);
        //}


        //return result;

    }

    /**
     * Obtain the patternId for a given op
     *
     * @param op
     * @return
     */
    public Node lookup(Op op) {
        // TODO
        return null;
    }


    public static <K> Table getTable(Cache<K, StorageEntry> cache, K key) {
        StorageEntry storageEntry = cache.getIfPresent(key);
        Table result = null;
        if(storageEntry != null) {
            RangedSupplier<Long, Binding> rangedSupplier = storageEntry.storage;

            @SuppressWarnings("unchecked")
            CacheRangeInfo<Long> cri = rangedSupplier.unwrap(CacheRangeInfo.class, true).get();

            Range<Long> atLeastZero = Range.atLeast(0l);
            boolean isAllCached = cri.isCached(atLeastZero);
            if(isAllCached) {
                Stream<Binding> bindings = rangedSupplier.apply(atLeastZero).toList().blockingGet().stream();
                Iterator<Binding> it = bindings.iterator();
                ResultSet rs = ResultSetUtils.create2(storageEntry.varInfo.getProjectVars(), it);
                result = TableUtils.createTable(rs);
                //substitute = OpTable.create(table);
            }
        }

        return result;
    }

    /**
     * Possibly new approach(wip):
     * The rewrite does not directly inject cache hits, but it returns a multimap
     * that associates nodes with hits.
     * This way it is possible to chose among the replacement candidates
     *
     *
     * Current approach:
     *
     * The rewrite creates a new algebra expression with view hits injected.
     * TODO For convenience, it may be usefule if a a list of the hits themselves was returned
     *
     *
     *
     */
    @Override
    public RewriteResult2 rewrite(ProjectedOp pop) {//Op normalizedOp) {

        // Since we are cutting the projection in the put method, we also have to cut it here
        //ProjectedOp pop = SparqlCacheUtils.cutProjectionAndNormalize(rawOp, opNormalizer);

        Op current = pop.getResidualOp();//normalizedOp;
        //ProjectedOp current = pop;

        //Op current = pop.getResidualOp(); // op;

        //Multimap<Op, LookupResult<Node>> nodeToReplacements = HashMultimap.create();

        int rewriteLevel = 0;

        boolean changed;
        do {
            changed = false;

            // Attempt to replace complete subtrees
            ProjectedOp currentPop = new ProjectedOp(pop.getProjection(), current);
            Map<Node, OpVarMap> lookupResults = viewMatcherTreeBased.lookup(currentPop);

            // Projection validation:
            //SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, vm)

//            if(lookupResults.isEmpty()) {
//                break;
//            }

            for(Entry<Node, OpVarMap> lr : lookupResults.entrySet()) {
                Node viewId = lr.getKey();
                OpVarMap opVarMap = lr.getValue();

                Map<Op, Op> opMap = opVarMap.getOpMap();
                Iterable<Map<Var, Var>> varMaps = opVarMap.getVarMaps();

                //Op viewRootOp = lr.getEntry().queryIndex.getOp();
                Op viewRootOp = viewMatcherTreeBased.getPattern(viewId).getResidualOp();

                Map<Var, Var> map = Iterables.getFirst(varMaps, null);

                if(map == null) {
                    continue;
                }

                Op userSubstOp = opMap.get(viewRootOp);

                // TODO Properly inject service references into the op node

                // Check the cache for whether the data associated with the view id
                // is complete

//                StorageEntry storageEntry = cache.getIfPresent(viewId);
//                if(storageEntry != null) {
//                	RangedSupplier<Long, Binding> rangedSupplier = storageEntry.storage;
//
//                	@SuppressWarnings("unchecked")
//					CacheRangeInfo<Long> cri = rangedSupplier.unwrap(CacheRangeInfo.class, true);
//
//                	Range<Long> atLeastZero = Range.atLeast(0l);
//                	boolean isAllCached = cri.isCached(atLeastZero);
//                	if(isAllCached) {
//                		ClosableIterator<Binding> bindings = rangedSupplier.apply(atLeastZero);
//                		ResultSet rs = ResultSetUtils.create2(storageEntry.varInfo.getProjectVars(), bindings);
//                		Table table = TableUtils.createTable(rs);
//                		substitute = OpTable.create(table);
//                	}
//                }
//

                // Do not perform substitution with a table on the root node so that
                // we can evaluate slices on the rangedSupplier rather than having jena
                // to evaluate a SPARQL query
                // TODO We may evaluate whether this makes a significant difference in performance
                // - the only time we save is having to copy data from the supplier to the table object

                boolean isRoot = userSubstOp == current;

                // if the root node was mapped, we have a complete match, indicated by rewriteLevel 2
                rewriteLevel = Math.max(rewriteLevel, isRoot ? 2 : 1);


                Op substitute = null;
                if(!isRoot) {
                    Table table = getTable(cache, viewId);
                    substitute = table == null ? null : OpTable.create(table);
                }

                // If substitute is null, create a default substitute
                if(substitute == null) {
                    // Get the node in the user query which to replace
                    substitute = new OpService(viewId, OpNull.create(), true);

                    //substitute = null;
                }

                // Apply substitution (if substitute is not null)
                if(substitute != null) {
                    substitute = OpUtils.wrapWithProjection(substitute, map);

                    current = OpUtils.substitute(current, userSubstOp, substitute);
                    changed = true;
                }


                logger.debug("Rewrite after substitution: " + current);
            }
        } while(changed);


        // Find further substitution candidates for all (canonical) quad pattern leafs
        Tree<Op> tree = OpUtils.createTree(current);
        List<Op> leafs = TreeUtils.getLeafs(tree);

        // Determine the rewrite level:
        // If there is just a single leaf and there is no replacement pattern (i.e. every quad of the query was covered by one or more views)
        // then we have a complete rewrite.
        // TODO We can distinguish between complete rewrites where the result is pattern free (no more remote calls), and
        // and those where there is only a single operation left

        boolean hasRewrite = false;
        boolean isCompleteRewrite = true;
        for(Op rawLeafOp : leafs) {
            if(rawLeafOp instanceof OpExtConjunctiveQuery) {
            //Op effectiveOp = leafOp instanceof OpExtQuadFilterPatternCanonical ? ((OpExt)leafOp).effectiveOp() : leafOp;
                OpExtConjunctiveQuery leafOp = (OpExtConjunctiveQuery)rawLeafOp;
                //QuadFilterPatternCanonical qfpc = leafOp.getQfpc();
                ConjunctiveQuery userCq = leafOp.getQfpc();

//                Op effectiveOp = leafOp.effectiveOp();
//
//                Set<Var> availableVars = OpVars.visibleVars(effectiveOp);
//                VarUsage varUsage = OpUtils.analyzeVarUsage(tree, leafOp, availableVars);

                //logger.debug("VarUsage: " + varUsage);

                Map<Node, QfpcMatch> hits = viewMatcherQuadPatternBased.lookup(userCq);

                logger.debug("Got " + hits.size() + " hits for lookup with " + userCq);

                // Only retain hits for which we actually have complete cache data
                Map<Node, Table> hitData = hits.entrySet().stream()
                        .map(hit -> new SimpleEntry<>(hit.getKey(), getTable(cache, hit.getKey())))
                        .filter(x -> x.getValue() != null)
                        .collect(Collectors.toMap(
                                x -> x.getKey(), x -> x.getValue()));

                // Filter hits by those having associated data
                hits = hits.entrySet().stream()
                        .filter(x -> hitData.containsKey(x.getKey()))
                        .collect(Collectors.toMap(
                                Entry::getKey,
                                Entry::getValue,
                                (x, y) -> { throw new AssertionError(); },
                                LinkedHashMap::new));

                // Remove subsumed hits
                hits = SparqlViewMatcherQfpcImpl.filterSubsumption(hits);

                // Aggregate
                QfpcAggMatch<Node> agg = SparqlViewMatcherQfpcImpl.aggregateResults(hits);
                if(agg != null) {
                    hasRewrite = true;

                    List<Op> ops = new ArrayList<>();

                    for(Entry<Node, QfpcMatch> hit : hits.entrySet()) {
                        Table table = hitData.get(hit.getKey());

                        Op xop = OpTable.create(table);
                        ops.add(xop);
                    }

                    QuadFilterPatternCanonical replacementPattern = agg.getReplacementPattern();
                    boolean hasNonEmptyRemainder = replacementPattern != null && !replacementPattern.isEmpty();

                    if(hasNonEmptyRemainder) {
                        ops.add(replacementPattern.toOp());
                    }

                    if(hasNonEmptyRemainder) {
                        isCompleteRewrite = false;
                    } else {

                    }

                    Op result;
                    if(ops.size() == 1) {
                        result = ops.get(0);
                    } else {
                        OpSequence r = OpSequence.create();
                        ops.forEach(r::add);
                        result = r;
                    }

                    current = OpUtils.substitute(current, rawLeafOp, result);
                }
            }
        }

        if(hasRewrite) {
            rewriteLevel = isCompleteRewrite ? 2 : 1;
        }


        current = opDenormalizer.rewrite(current);

        Map<Node, StorageEntry> storageMap = new HashMap<>();

        //ProjectedOp cp = new ProjectedOp(pop.getProjection(), pop.isDistinct(), current);

        //RewriteResult2 result = new RewriteResult2(current, storageMap, rewriteLevel);
        RewriteResult2 result = new RewriteResult2(current, storageMap, rewriteLevel);
        return result;
    }



//
//                Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQfpc);
//
//                candidateSolutions.filter(
//                		map -> SparqlViewCacheImpl.validateCandidateByProjectedVars(viewQfpc, queryQfpc, varMap, candVarCombo)
//                );


//SparqlQueryContainmentUtils.tryMatch(viewQfpc, queryQfpc)
//                QfpcMatch match;
//                		SparqlViewCacheImpl.postProcessResult(actualResult);
//			postProcessResult(List<QfpcMatch> actualResult) {




                //lr.getReplacementPattern()



                // TODO Use the SparqlCacheUtils to get the set of covers for a qfpc
                //SparqlCacheUtils.


                //viewMatcherQuadPatternBased

                // If we decide to cache the leaf as a whole, we just have to do
                //viewMatcherQuadPatternBased.put(leafOp, cacheOp);


//	    		ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(op);
//	    		if(pqfp != null) {
//
//	    			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(pqfp.getQuadFilterPattern(), VarGeneratorImpl2.create());
//
//
//
//	    			//viewMatcherQuadPatternBased.
//

    public Op finalizeRewrite(Op op) {
        // TODO Adding the overall caching op has to be done AFTER the complete rewrite - i.e. here is the WRONG place

        Map<Node, StorageEntry> storageMap = new HashMap<>();

        // Prepare a storage for the original rawOp
        QueryExecutionFactory qef = null;
        ExecutorService executorService = null;

        OpExecutor opExecutor = null;
        Op rootOp = null;
        Range<Long> cacheRange = Range.atMost(100000l);
        Context context = null;
        RangedSupplierLazyLoadingListCache<Binding> storage = new RangedSupplierLazyLoadingListCache<Binding>(executorService, new RangedSupplierOp(rootOp, context), cacheRange, null);

        Node storageRef = null;
        VarInfo varInfo = new VarInfo(OpVars.visibleVars(rootOp), 0); //Collections.emptySet());
        StorageEntry storageEntry = new StorageEntry(storage, varInfo);

        storageMap.put(storageRef, storageEntry);

        // Add an operation to cache the whole result
        Op superRootOp = new OpService(storageRef, rootOp, false);



        return superRootOp;
        // TODO Inject values directly if the data is local and comparatively small
//    	Range<Long> range;
//    	storage.isCached(range);


    }

    //public static Op rewrite



//
//	@Override
//	public boolean acceptsAdd(Op op) {
//		// TODO Auto-generated method stub
//		return false;
//	}



}
