package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierSubRange;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.cache.Cache;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;

public class QueryExecutionViewMatcherMaster
	extends QueryExecutionBaseSelect
{
	protected OpRewriteViewMatcherStateful opRewriter;
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
    		OpRewriteViewMatcherStateful opRewriter
    ) {
    	super(query, subFactory);

    	this.opRewriter = opRewriter;
    	this.context = ARQ.getContext();
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

    @Override
    protected ResultSetCloseable executeCoreSelect(Query rawQuery) {

    	List<Var> projectVars = rawQuery.getProjectVars();
    	List<String> projectVarNames = VarUtils.getVarNames(projectVars);

    	// Store a present slice, but remove it from the query
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



//    	Node serviceNode = NodeFactory.createURI("view://test.org");
//
//    	rewrittenOp = new OpService(serviceNode, OpNull.create(), false);
//
//    	RangedSupplier<Long, Binding> backend = new RangedSupplierQuery(parentFactory, rawQuery);
//    	//RangedSupplierLazyLoadingListCache<Binding>
//    	RangedSupplier<Long, Binding> storage = new RangedSupplierLazyLoadingListCache<>(executorService, backend, Range.atMost(10000l), null);
//
//    	storage = RangedSupplierSubRange.create(storage, range);



    	ExecutorService executorService = Executors.newCachedThreadPool();

    	Set<Var> visibleVars = OpVars.visibleVars(rewrittenOp);
    	VarInfo varInfo = new VarInfo(visibleVars, Collections.emptySet());

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



    	System.out.println("Rewritten op being passed to execution:\n" + rewrittenOp);
    	System.out.println("Rewritten op being passed to execution:\n" + rawQuery);

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

    	Context ctx = context.copy();
//    	context.put(OpExecutorViewCache.STORAGE_MAP, storageMap);


    	Cache<Node, StorageEntry> cache = opRewriter.getCache();

    	RangedSupplier<Long, Binding> s2 = new RangedSupplierOp(rewrittenOp, ctx);
    	if(cacheWholeQuery) {
    		// Caching the whole query requires the following actions:
    		// (1) Allocate a new id for the query
    		// (2) Create a storage entry for the rewritten entry
    		// (3) Make the new id of the query together with its original (i.e. non-rewritten) op known to the rewriter
    		Node id = NodeFactory.createURI("view://ex.org/view" + queryOp.hashCode());

        	s2 = new RangedSupplierLazyLoadingListCache<Binding>(executorService, s2, Range.closedOpen(0l, 10000l));

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

