package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCacheImpl;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
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

import com.google.common.collect.Range;

class ViewMatcherData {
	boolean isRangeCache(Range<Long> range) {
		return true;
	}

	public Set<Var> getDefinedVars() {
		return null;
	}
}

class VarInfo {
	public Set<Var> projectVars;
	public Set<Var> distinctVars;

	public VarInfo(Set<Var> projectVars, Set<Var> distinctVars) {
		super();
		this.projectVars = projectVars;
		this.distinctVars = distinctVars;
	}

}


class StorageEntry {
	public StorageEntry(RangedSupplierLazyLoadingListCache<Binding> storage, VarInfo varInfo) {
		super();
		this.storage = storage;
		this.varInfo = varInfo;
	}

	public RangedSupplierLazyLoadingListCache<Binding> storage;
	public VarInfo varInfo;
}


/**
 * Rewrite an algebra expression under view matching
 *
 * @author raven
 *
 */
public class OpRewriteViewMatcher
	implements Rewrite
{
	protected Rewrite opNormalizer;
	protected OpViewMatcher<Node> viewMatcherTreeBased;
	protected SparqlViewCache<Node> viewMatcherQuadPatternBased;

	// Mapping from cache entry id to the cache data
	// - We need to be able to ask whether a certain range is completely in cache
	// - Ask which variables are defined by the cache data
	// -
	protected Map<Node, ViewMatcherData> idToCacheData;


	public OpRewriteViewMatcher() {
		this.opNormalizer = OpViewMatcherTreeBased::normalizeOp;
		this.viewMatcherTreeBased = OpViewMatcherTreeBased.create();
		this.viewMatcherQuadPatternBased = new SparqlViewCacheImpl<>();
	}

	//@Override
	// TODO Do we need a further argument for the variable information?
	public void put(Op op, Node id) {

    	Op normalizedOp = opNormalizer.rewrite(op);

    	// Allocate a new id entry of this op
    	//Node result = NodeFactory.createURI("id://" + StringUtils.md5Hash("" + normalizedItem));

    	// TODO:

		ProjectedQuadFilterPattern conjunctiveQuery = SparqlCacheUtils.transform(normalizedOp);
		if(conjunctiveQuery != null) {
			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(conjunctiveQuery.getQuadFilterPattern(), VarGeneratorImpl2.create());

			viewMatcherQuadPatternBased.put(qfpc, id);
		} else {
			viewMatcherTreeBased.put(normalizedOp, id);
		}

		//return result;

	}


	/**
	 * The rewrite creates a new algebra expression with view hits injected.
	 * For convenience, we should also return a list of the hits themselves.
	 *
	 *
	 *
	 */
	@Override
	public Op rewrite(Op rawOp) {
    	Op op = opNormalizer.rewrite(rawOp);


    	Op current = op;
    	for(;;) {
			// Attempt to replace complete subtrees
			Collection<LookupResult> lookupResults = viewMatcherTreeBased.lookup(op);

			if(lookupResults == null) {
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
			}
    	}


		// Find further substitution candidates for all (canonical) quad pattern leafs
    	Tree<Op> tree = OpUtils.createTree(current);
    	List<Op> leafs = TreeUtils.getLeafs(tree);


    	for(Op leafOp : leafs) {
    		VarUsage varUsage = OpUtils.analyzeVarUsage(tree, leafOp);


    		ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(op);
    		if(pqfp != null) {

    			QuadFilterPatternCanonical qfpc = SparqlCacheUtils.canonicalize2(pqfp.getQuadFilterPattern(), VarGeneratorImpl2.create());



    			//viewMatcherQuadPatternBased.

    		}


    	}

    	Map<Node, StorageEntry> storageMap = new HashMap<>();

    	// Prepare a storage for the original rawOp
    	QueryExecutionFactory qef = null;
    	ExecutorService executorService = null;

    	OpExecutor opExecutor = null;
    	Op rootOp = null;
    	Range<Long> cacheRange = Range.atMost(100000l);
    	RangedSupplierLazyLoadingListCache<Binding> storage = new RangedSupplierLazyLoadingListCache<Binding>(executorService, new RangedSupplierOp(opExecutor, rootOp), cacheRange, null);

    	Node storageRef = null;
    	VarInfo varInfo = new VarInfo(OpVars.visibleVars(rootOp), Collections.emptySet());
    	StorageEntry storageEntry = new StorageEntry(storage, varInfo);

    	storageMap.put(storageRef, storageEntry);

    	// Add an operation to cache the whole result
    	Op superRootOp = new OpService(storageRef, rootOp, false);



    	// TODO Inject values directly if the data is local and comparatively small
//    	Range<Long> range;
//    	storage.isCached(range);


		// TODO Auto-generated method stub
		return null;
	}


	//public static Op rewrite



//
//	@Override
//	public boolean acceptsAdd(Op op) {
//		// TODO Auto-generated method stub
//		return false;
//	}



}
