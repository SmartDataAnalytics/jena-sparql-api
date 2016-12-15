package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.QfpcMatch;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpcImpl;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpExtQuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.util.collection.CacheRangeInfo;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOp;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
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
    protected Rewrite opNormalizer;
    protected Rewrite opDenormalizer;

    protected SparqlViewMatcherOp<Node> viewMatcherTreeBased;
    protected SparqlViewMatcherQfpc<Node> viewMatcherQuadPatternBased;


    protected Map<Node, Map<Node, VarInfo>> patternIdToStorageIdToVarInfo;
    protected Map<Node, Node> storageIdToPatternId;

    // Maps result set ids to storage ids
    protected Cache<Node, StorageEntry> cache;
    //protected Map<Node, StorageEntry> storageMap = new HashMap<>();


    // Mapping from cache entry id to the cache data
    // - We need to be able to ask whether a certain range is completely in cache
    // - Ask which variables are defined by the cache data
    // -
    //protected Map<Node, ViewMatcherData> idToCacheData;


    public OpRewriteViewMatcherStateful(Cache<Node, StorageEntry> cache, Collection<RemovalListener<Node, StorageEntry>> removalListeners) {
        this.opNormalizer = SparqlViewMatcherOpImpl::normalizeOp;
        this.opDenormalizer = SparqlViewMatcherOpImpl::denormalizeOp;
        this.viewMatcherTreeBased = SparqlViewMatcherOpImpl.create();
        this.viewMatcherQuadPatternBased = new SparqlViewMatcherQfpcImpl<>();
        this.cache = cache;


        this.patternIdToStorageIdToVarInfo = new HashMap<>();
        this.storageIdToPatternId = new HashMap<>();

        removalListeners.add((n)-> removeStorage(n.getKey()));
    }

    public void removeStorage(Node storageKey) {
        Node patternId = storageIdToPatternId.computeIfPresent(storageKey, (k, v) -> null);

        viewMatcherTreeBased.removeKey(patternId);
        viewMatcherQuadPatternBased.removeKey(patternId);

        // TODO Maybe there is potential to optimize with another computeIfPresent
        Map<Node, VarInfo> map = patternIdToStorageIdToVarInfo.get(patternId);
        map.remove(storageKey);
        if(patternIdToStorageIdToVarInfo.isEmpty()) {
            storageIdToPatternId.remove(storageKey);
        }
    }


    public Cache<Node, StorageEntry> getCache() {
        return cache;
    }



    //@Override
    // TODO Do we need a further argument for the variable information?
    public void put(Node storageId, Op op) {

        Op normalizedOp = opNormalizer.rewrite(op);


        // TODO: Finish cutting away the projection
        ProjectedOp projectedOp = SparqlCacheUtils.cutProjection(normalizedOp);
        // TODO Hack to map between ProjectedOp and VarInfo - make this consistent!
        VarInfo varInfo;
        if(projectedOp != null) {
            varInfo = new VarInfo(new HashSet<>(projectedOp.getProjection().getVars()), 0);//Collections.emptySet());
            normalizedOp = projectedOp.getResidualOp();
        } else {
            throw new RuntimeException("todo handle this case");
            //OpVars.visibleVars(normalizedOp);
        }

        // TODO Check if the pattern we are putting is isomorphic to an existing one
        Node patternId = lookup(normalizedOp);


        Map<Node, VarInfo> map = patternIdToStorageIdToVarInfo.computeIfAbsent(patternId,  (n) -> new HashMap<>());
        map.put(storageId, varInfo);

        storageIdToPatternId.put(storageId, patternId);


        // Allocate a new id entry of this op
        //Node result = NodeFactory.createURI("id://" + StringUtils.md5Hash("" + normalizedItem));

        // TODO Verify: Transform to qfpc directly (the normalizedOp has the projection cut away)
        //ProjectedQuadFilterPattern conjunctiveQuery = SparqlCacheUtils.transform(normalizedOp);
        QuadFilterPattern qfp = SparqlCacheUtils.extractQuadFilterPattern(op);

        if(qfp != null) {
            QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(qfp, VarGeneratorImpl2.create());

            viewMatcherQuadPatternBased.put(storageId, qfpc);
        }

        // Always add the whole op
        //else {
            viewMatcherTreeBased.put(storageId, normalizedOp);
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
        		Stream<Binding> bindings = rangedSupplier.apply(atLeastZero);
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
    public RewriteResult2 rewrite(Op rawOp) {

        Op op = opNormalizer.rewrite(rawOp);


        Op current = op;

        //Multimap<Op, LookupResult<Node>> nodeToReplacements = HashMultimap.create();

        boolean changed = true;
        while(changed) {
            // Attempt to replace complete subtrees
            Collection<LookupResult<Node>> lookupResults = viewMatcherTreeBased.lookup(current);

//            if(lookupResults.isEmpty()) {
//                break;
//            }

            changed = false;
            for(LookupResult<Node> lr : lookupResults) {
                OpVarMap opVarMap = lr.getOpVarMap();

                Map<Op, Op> opMap = opVarMap.getOpMap();
                Iterable<Map<Var, Var>> varMaps = opVarMap.getVarMaps();

                Node viewId = lr.getEntry().id;
                Op viewRootOp = lr.getEntry().queryIndex.getOp();
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
                    current = OpUtils.substitute(current, userSubstOp, substitute);
                    changed = true;
                }


                logger.debug("Rewrite after substitution: " + current);
            }
        }


        // Find further substitution candidates for all (canonical) quad pattern leafs
        Tree<Op> tree = OpUtils.createTree(current);
        List<Op> leafs = TreeUtils.getLeafs(tree);


        for(Op rawLeafOp : leafs) {
            if(rawLeafOp instanceof OpExtQuadFilterPatternCanonical) {
            //Op effectiveOp = leafOp instanceof OpExtQuadFilterPatternCanonical ? ((OpExt)leafOp).effectiveOp() : leafOp;
                OpExtQuadFilterPatternCanonical leafOp = (OpExtQuadFilterPatternCanonical)rawLeafOp;
                QuadFilterPatternCanonical qfpc = leafOp.getQfpc();

                Op effectiveOp = leafOp.effectiveOp();

                Set<Var> availableVars = OpVars.visibleVars(effectiveOp);
                VarUsage varUsage = OpUtils.analyzeVarUsage(tree, leafOp, availableVars);

                logger.debug("VarUsage: " + varUsage);

                Collection<QfpcMatch<Node>> hits = viewMatcherQuadPatternBased.lookup(qfpc);

                // Only retain hits for which we actually have complete cache data
                Map<Node, Table> hitData = hits.stream()
                		.map(hit -> new SimpleEntry<>(hit.getTable(), getTable(cache, hit.getTable())))
                		.filter(x -> x.getValue() != null)
                		.collect(Collectors.toMap(
                				x -> x.getKey(), x -> x.getValue()));

                // Filter hits by those having associated data
                hits = hits.stream()
                		.filter(x -> hitData.containsKey(x.getTable()))
                		.collect(Collectors.toList());

                // Remove subsumed hits
                hits = SparqlViewMatcherQfpcImpl.filterSubsumption(hits);

                // Aggregate
                QfpcAggMatch<Node> agg = SparqlViewMatcherQfpcImpl.aggregateResults(hits);
                if(agg != null) {


	                List<Op> ops = new ArrayList<>();

	                for(QfpcMatch<Node> hit : hits) {
	                	Table table = hitData.get(hit.getTable());

	                	Op xop = OpTable.create(table);
	                	ops.add(xop);
	                }

	                QuadFilterPatternCanonical replacementPattern = agg.getReplacementPattern();
	                if(replacementPattern != null && !replacementPattern.isEmpty()) {
	                	ops.add(replacementPattern.toOp());
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


        current = opDenormalizer.rewrite(current);

        Map<Node, StorageEntry> storageMap = new HashMap<>();

        RewriteResult2 result = new RewriteResult2(current, storageMap);
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
