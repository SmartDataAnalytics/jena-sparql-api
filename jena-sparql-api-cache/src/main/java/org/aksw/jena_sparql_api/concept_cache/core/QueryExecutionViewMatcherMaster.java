package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.algebra.transform.TransformPushSlice;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.util.collection.CacheRangeInfo;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ClosableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.collect.Range;

public class QueryExecutionViewMatcherMaster
	extends QueryExecutionBaseSelect
{
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionViewMatcherMaster.class);

	protected OpRewriteViewMatcherStateful opRewriter;
	protected ExecutorService executorService;
	//protected Map<Node, StorageEntry> storageMap;

	// The jena context - used for setting up cache entries
	// TODO Not sure if this was better part of the rewriter - or even a rewriteContext object
	protected Context context;

	// TODO Maybe add a decider which determines whether the result set of a query should be cached


	//protected Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier;
	//protected ExecutorService executorService;
	//protected OpViewMatcher viewMatcher;



    protected long indexResultSetSizeThreshold;

    public QueryExecutionViewMatcherMaster(
    		Query query,
    		QueryExecutionFactory subFactory,
    		OpRewriteViewMatcherStateful opRewriter,
    		ExecutorService executorService
    ) {
    	super(query, subFactory);

    	this.opRewriter = opRewriter;
    	this.context = ARQ.getContext();
    	this.executorService = executorService;
    }


    public static ResultSetCloseable createResultSet(List<String> varNames, RangedSupplier<Long, Binding> rangedSupplier, Range<Long> range, Map<Var, Var> varMap) {
    	ClosableIterator<Binding> it = rangedSupplier.apply(range);
    	Iterable<Binding> tmp = () -> it;
    	Stream<Binding> stream = StreamSupport.stream(tmp.spliterator(), false);
    	if(varMap != null) {
    		stream = stream.map(b -> BindingUtils.rename(b, varMap));
    	}
    	stream = stream.onClose(() -> it.close());

    	//List<String> varNames = query.getResultVars();
    	//ResultSetCloseable result = ResultSetUtils.create(varNames, it);
    	ResultSet rs = ResultSetUtils.create(varNames, stream.iterator());
    	ResultSetCloseable result = new ResultSetCloseable(rs);

    	return result;
    }

    /**
     * Substitute cache references either with the cached data or -
     * if that is not completely available - substitute with the original expression.
     *
     * @param op
     * @param tree
     * @return
     */


    @Override
    protected ResultSetCloseable executeCoreSelect(Query rawQuery) {

    	List<Var> projectVars = rawQuery.getProjectVars();
    	//List<String> projectVarNames = VarUtils.getVarNames(projectVars);

    	// Store a present slice, but remove it from the query
    	// NOTE This could also be done on the OP level; but
    	// it seems to be much more convenient to be done on the query level
    	Range<Long> range = QueryUtils.toRange(query);
    	Query q = query.cloneQuery();
    	q.setLimit(Query.NOLIMIT);
    	q.setOffset(Query.NOLIMIT);

    	Op queryOp = Algebra.toQuadForm(Algebra.compile(q));




    	// The thing here is, that in general we need to
    	// - Initialize the execution context / jena-wise global data
    	// - Perform the rewrite (may affect execution context state)
    	// - Clean up the execution context / jena-wise global data
    	RewriteResult2 rr = opRewriter.rewrite(queryOp);
    	Op rewrittenOp = rr.getOp();

    	Map<Node, StorageEntry> storageMap = rr.getIdToStorageEntry();


    	// Map<Node, List<


    	// All subtrees that are to be executed on the original data source must be wrapped with
		// a standard sparql service clause

		Tree<Op> tree = OpUtils.createTree(rewrittenOp);

		// Reverse the levels, so that we start with the leafs
		Predicate<Op> predicate = x -> !(x instanceof OpService);

		Set<Op> taggedNodes = TreeUtils.propagateBottomUpLabel(tree, predicate);

		logger.debug("Tagged: " + taggedNodes);

		// If we tagged the root node, then everything can be executed on the original service


		int idX = 0;
		// Remap all tagged nodes to be executed on the original service
		Map<Op, Op> taggedToService = new IdentityHashMap<>();
		for(Op tag : taggedNodes) {
			Query query = OpAsQuery.asQuery(tag);

			Node serviceNode = NodeFactory.createURI("view://service/" + idX++);
			Op serviceOp = new OpService(serviceNode, OpNull.create(), false);

			RangedSupplier<Long, Binding> s3 = new RangedSupplierQuery(parentFactory, query);

			VarInfo varInfo = new VarInfo(new HashSet<>(query.getProjectVars()), 0);
			StorageEntry se = new StorageEntry(s3, varInfo); // The var info is not used
			storageMap.put(serviceNode, se);

			taggedToService.put(tag, serviceOp);
		}

		rewrittenOp = OpUtils.substitute(rewrittenOp, false, taggedToService::get);

		// Adujst limit
		rewrittenOp = QueryUtils.applyRange(rewrittenOp, range);

    	Transform transform = new TransformPushSlice();
    	Op tmp;
    	do {
        	tmp = rewrittenOp;
    		rewrittenOp = Transformer.transform(transform, tmp);

    	} while(!rewrittenOp.equals(tmp));

    	logger.debug("Raw query being rewritten for execution:\n" + rawQuery);
    	logger.debug("Rewritten op being passed to execution:\n" + rewrittenOp);



		//rewrittenOp = new OpSlice(rew)

    	Context ctx = context.copy();
    	ctx.put(OpExecutorViewCache.STORAGE_MAP, storageMap);

    	RangedSupplier<Long, Binding> s2;
    	s2 = new RangedSupplierOp(rewrittenOp, ctx);



    		// For each parents of which all children are in the set, remove the children from the set
    		// and add the parent to the set instead




//    	Node serviceNode = NodeFactory.createURI("view://test.org");
//
//    	rewrittenOp = new OpService(serviceNode, OpNull.create(), false);
//
//    	RangedSupplier<Long, Binding> backend = new RangedSupplierQuery(parentFactory, rawQuery);
//    	//RangedSupplierLazyLoadingListCache<Binding>
//    	RangedSupplier<Long, Binding> storage = new RangedSupplierLazyLoadingListCache<>(executorService, backend, Range.atMost(10000l), null);
//
//    	storage = RangedSupplierSubRange.create(storage, range);



    	Set<Var> visibleVars = new HashSet<>(projectVars);//OpVars.visibleVars(rewrittenOp);
    	VarInfo varInfo = new VarInfo(visibleVars, 0);

//    	if(false) {
//	    	Iterators.size(storage.apply(range));
//	    	@SuppressWarnings("unchecked")
//			RangedSupplierLazyLoadingListCache<Binding> test = storage.unwrap(RangedSupplierLazyLoadingListCache.class, true);
//	    	System.out.println("Is range cached: " + test.isCached(range));
//
//	    	ResultSet xxx = ResultSetUtils.create2(visibleVars, storage.apply(range));
//	    	Table table = TableUtils.createTable(xxx);
//	    	OpTable repl = OpTable.create(table);
//	    	rewrittenOp = repl;
//    	}



//    	StorageEntry se = new StorageEntry(storage, varInfo);
//    	storageMap.put(serviceNode, se);



//


    	// Note: We use Jena to execute the op.
    	// The op itself may use SERVICE<> as the root node, which will cause jena to pass execution to the appropriate handler

    	// TODO Pass the op to an op executor
    	//QueryEngineMainQuad

    	// TODO Decide whether to cache the overall query
    	// Do NOT cache if:
    	// - there is already a cache entry that only differs in the var map
    	// - (if the new query is just linear post processing of an existing cache entry)

    	// This means, that the query will be available for cache lookups
    	boolean cacheWholeQuery = true;

//    	context.put(OpExecutorViewCache.STORAGE_MAP, storageMap);


    	Cache<Node, StorageEntry> cache = opRewriter.getCache();

    	if(cacheWholeQuery) {
    		// Caching the whole query requires the following actions:
    		// (1) Allocate a new id for the query
    		// (2) Create a storage entry for the rewritten entry
    		// (3) Make the new id of the query together with its original (i.e. non-rewritten) op known to the rewriter
    		Node id = NodeFactory.createURI("view://ex.org/view" + queryOp.hashCode());

        	s2 = new RangedSupplierLazyLoadingListCache<Binding>(executorService, s2, Range.closedOpen(0l, 10000l));
    		//s2 = new RangedSupplierLazyLoadingListCache<Binding>(executorService, s2, range);

        	StorageEntry se2 = new StorageEntry(s2, varInfo);

    		//storageMap.put(id, se2);

        	// TODO The registration at the cache and the rewriter should be atomic
        	// At least we need to deal with the chance that the rewriter maps an op to an id for
        	// which the storageEntry has not yet been registered at the cache
    		opRewriter.put(id, queryOp);
        	cache.put(id, se2);
    	} else {
    		// Nothing todo - just create a result set from s2
    	}


    	List<String> visibleVarNames = VarUtils.getVarNames(visibleVars);

    	ResultSetCloseable result = createResultSet(visibleVarNames, s2, range, null);

    	return result;


//    	DatasetGraph dg = DatasetGraphFactory.create();
//    	Context context = ARQ.getContext().copy();
//    	context.put(OpExecutorViewCache.STORAGE_MAP, storageMap);
//    	QueryEngineFactory qef = QueryEngineRegistry.get().find(rewrittenOp, dg, context);
//    	Plan plan = qef.create(rewrittenOp, dg, BindingRoot.create(), context);
//    	QueryIterator queryIter = plan.iterator();
//
//
//    	//QueryIterator queryIter = x.eval(rewrittenOp, dg, BindingRoot.create(), context);
//    	ResultSet tmpRs = ResultSetFactory.create(queryIter, projectVarNames);
//
//    	// TODO Not sure if we should really return a result set, or a QueryIter instead
//    	ResultSetCloseable result = new ResultSetCloseable(tmpRs, () -> queryIter.close());




    	//ResultSetUtils.create(varNames, bindingIt)

    	//QueryEngineMain
    	//QC.execute(rewrittenOp, BindingRoot.create(), ARQ.getContext());

    	//org.apache.jena.query.QueryExecutionFactory.create(queryStr, syntax, model, initialBinding)
//
//
//
//    	LookupResult<Node> lr = viewMatcher.lookupSingle(opCache);
//        RangedSupplier<Long, Binding> rangedSupplier;
//        Map<Var, Var> varMap;
//        if(lr == null) {
//            Node id = viewMatcher.add(opCache);
//        	// Obtain the supplier from a factory (the factory may e.g. manage the sharing of a thread pool)
//
//            rangedSupplier = new RangedSupplierQuery(parentFactory, query);
//        	rangedSupplier = new RangedSupplierLazyLoadingListCache<>(executorService, rangedSupplier, Range.atMost(10000l), null);
//
//        	//rangedSupplier = new RangedSupplierQuery(parentFactory, q);
//        	opToRangedSupplier.put(id, rangedSupplier);
//        	varMap = null;
//        }
//        else {
//
//            varMap = Iterables.getFirst(lr.getOpVarMap().getVarMaps(), null);
//
//            assert varMap != null : "VarMap was not expected to be null at this point";
//
//        	Node entryId = lr.getEntry().id;
//        	rangedSupplier = opToRangedSupplier.get(entryId);
//        }
//
//        ResultSetCloseable result = createResultSet(rangedSupplier, range, varMap);
//        return result;
    }

//
//    public static StorageEntry createStorageEntry(Op op, VarInfo varInfo, Context context) {
//    	//Set<Var> visibleVars = OpVars.visibleVars(op);
//    	VarInfo varInfo = new VarInfo(visibleVars, Collections.emptySet());
//
//    	RangedSupplier<Long, Binding> storage = new RangedSupplierOp(op, context);
//
//    	StorageEntry result = new StorageEntry(storage, varInfo);
//    	return result;
//
////    	@SuppressWarnings("unchecked")
////		RangedSupplierLazyLoadingListCache<Binding> test = storage.unwrap(RangedSupplierLazyLoadingListCache.class, true);
////    	System.out.println("Is range cached: " + test.isCached(range));
//
////    	ResultSet xxx = ResultSetUtils.create2(visibleVars, storage.apply(range));
////    	Table table = TableUtils.createTable(xxx);
////    	OpTable repl = OpTable.create(table);
////    	rewrittenOp = repl;
//
//
//    }
//
	@Override
	protected QueryExecution executeCoreSelectX(Query query) {
		// TODO Fix bad design - this method is not needed
		return null;
	}

}

