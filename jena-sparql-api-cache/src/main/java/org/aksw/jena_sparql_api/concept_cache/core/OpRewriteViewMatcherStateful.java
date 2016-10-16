package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCacheImpl;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpExtQuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.views.index.LookupResult;
import org.aksw.jena_sparql_api.views.index.OpViewMatcher;
import org.aksw.jena_sparql_api.views.index.OpViewMatcherTreeBased;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.util.Context;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
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
	protected Rewrite opNormalizer;
	protected OpViewMatcher<Node> viewMatcherTreeBased;
	protected SparqlViewCache<Node> viewMatcherQuadPatternBased;


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
		this.opNormalizer = OpViewMatcherTreeBased::normalizeOp;
		this.viewMatcherTreeBased = OpViewMatcherTreeBased.create();
		this.viewMatcherQuadPatternBased = new SparqlViewCacheImpl<>();
		this.cache = cache;

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
        	varInfo = new VarInfo(new HashSet<>(projectedOp.getProjection().getVars()), Collections.emptySet());
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
		} else {
			viewMatcherTreeBased.put(storageId, normalizedOp);
		}


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


	/**
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
    	for(;;) {
			// Attempt to replace complete subtrees
			Collection<LookupResult<Node>> lookupResults = viewMatcherTreeBased.lookup(current);

			if(lookupResults.isEmpty()) {
				break;
			}

			for(LookupResult<Node> lr : lookupResults) {
				OpVarMap opVarMap = lr.getOpVarMap();

				Map<Op, Op> opMap = opVarMap.getOpMap();
				Iterable<Map<Var, Var>> varMaps = opVarMap.getVarMaps();

				Node viewId = lr.getEntry().id;
				Op viewRootOp = lr.getEntry().queryIndex.getOp();
				Map<Var, Var> map = Iterables.getFirst(varMaps, null);

				// TODO Properly inject service references into the op node


				// Get the node in the user query which to replace
				Op userSubstOp = opMap.get(viewRootOp);
				Op newNode = new OpService(viewId, new OpQuadBlock(), true);

				current = OpUtils.substitute(current, userSubstOp, newNode);
				System.out.println("Current: " + current);
			}
    	}


		// Find further substitution candidates for all (canonical) quad pattern leafs
    	Tree<Op> tree = OpUtils.createTree(current);
    	List<Op> leafs = TreeUtils.getLeafs(tree);


    	for(Op rawLeafOp : leafs) {
    		if(rawLeafOp instanceof OpExtQuadFilterPatternCanonical) {
    		//Op effectiveOp = leafOp instanceof OpExtQuadFilterPatternCanonical ? ((OpExt)leafOp).effectiveOp() : leafOp;
    			OpExtQuadFilterPatternCanonical leafOp = (OpExtQuadFilterPatternCanonical)rawLeafOp;

    			Op effectiveOp = leafOp.effectiveOp();

	    		Set<Var> availableVars = OpVars.visibleVars(effectiveOp);
	    		VarUsage varUsage = OpUtils.analyzeVarUsage(tree, leafOp, availableVars);

	    		System.out.println("VarUsage: " + varUsage);


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
    		}


    	}


		// TODO Auto-generated method stub
    	Map<Node, StorageEntry> storageMap = new HashMap<>();

    	RewriteResult2 result = new RewriteResult2(rawOp, storageMap);
		return result;
	}


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
    	VarInfo varInfo = new VarInfo(OpVars.visibleVars(rootOp), Collections.emptySet());
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
