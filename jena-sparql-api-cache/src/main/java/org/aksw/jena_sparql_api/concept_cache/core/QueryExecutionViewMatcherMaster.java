package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedOp;
import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.collect.Range;

public class QueryExecutionViewMatcherMaster
    extends QueryExecutionBaseSelect
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionViewMatcherMaster.class);

    protected OpRewriteViewMatcherStateful opRewriter;
    protected ExecutorService executorService;

    // The jena context - used for setting up cache entries
    // TODO Not sure if this was better part of the rewriter - or even a rewriteContext object
    protected Context context;

    // TODO Maybe add a decider which determines whether the result set of a query should be cached

    protected long indexResultSetSizeThreshold;


    // Statistic attributes
    protected Double preparationTimeInSec;
    // null: not set, 0: miss, 1: partial, 2: complete
    protected Integer cacheHitLevel;

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
        Stream<Binding> stream = rangedSupplier.apply(range).toList().blockingGet().stream();
        if(varMap != null) {
            stream = stream.map(b -> BindingUtils.rename(b, varMap));
        }

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

    public static boolean isView(Op op) {
        boolean result = op instanceof OpService && isView(((OpService)op).getService());
        return result;
    }

    public static boolean isView(Node node) {
        boolean result = isView(node.getURI());
        return result;
    }

    public static boolean isView(String uri) {
        boolean result = uri.startsWith("view://");
        return result;
    }

    @Override
    protected ResultSetCloseable executeCoreSelect(Query rawQuery) {

        System.out.println("Query: " + rawQuery);

        Stopwatch sw = Stopwatch.createStarted();

        boolean cacheWholeQuery = true; //!rootService.getURI().startsWith("view://");


        boolean isDistinct = rawQuery.isDistinct();


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
//    	queryOp = SparqlViewMatcherOpImpl.normalizeOp(queryOp);


        // TODO opRewriter.lookup and opRewriter.put() both perform normalization
        // We could this duplicate processing by normalizing here
        // and passing the projected op to both functions
        ProjectedOp pop = AlgebraUtils.cutProjectionAndNormalize(queryOp, op -> QueryToGraph.normalizeOp(op, false));
        //List<Var> popVars = new ArrayList<>(pop.getProjection().getProjectVars());

        //Op coreQueryOp = pop.getResidualOp();

        System.out.println("Normalized: " + pop.getResidualOp());
        System.out.println("Round trip: " + OpAsQuery.asQuery(SparqlViewMatcherOpImpl.denormalizeOp(pop.getResidualOp())));

        // The thing here is, that in general we need to
        // - Initialize the execution context / jena-wise global data
        // - Perform the rewrite (may affect execution context state)
        // - Clean up the execution context / jena-wise global data
        RewriteResult2 rr = opRewriter.rewrite(pop);
        cacheHitLevel = rr.getRewriteLevel();
        Op rewrittenOp = rr.getOp();


        Map<Node, StorageEntry> storageMap = rr.getIdToStorageEntry();

        // All subtrees that are to be executed on the original data source must be wrapped with
        // a standard sparql service clause

        Tree<Op> tree = OpUtils.createTree(rewrittenOp);

        // Find all referenced views in the expression

        Set<Node> cacheRefs = TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren)
            .filter(QueryExecutionViewMatcherMaster::isView)
            .map(op -> ((OpService)op).getService())
            .collect(Collectors.toSet());

        Cache<Node, StorageEntry> cache = opRewriter.getCache();

        for(Node cacheRef : cacheRefs) {
            StorageEntry e = cache.getIfPresent(cacheRef);
            //RangedSupplier<Long, Binding> s = e.storage;

            storageMap.put(cacheRef, e);
        }


        // Reverse the levels, so that we start with the leafs
        Predicate<Op> predicate = x -> !(x instanceof OpService);

        Set<Op> taggedNodes = TreeUtils.propagateBottomUpLabel(tree, predicate);

        logger.debug("Tagged: " + taggedNodes);

        // If we tagged the root node, then everything can be executed on the original service


        int idX = 0;
        // Remap all tagged nodes to be executed on the original service
        Map<Op, Op> taggedToService = new IdentityHashMap<>();

        // Track whether we created a new service for the root node
        Node newRootServiceNode = null;


        for(Op tag : taggedNodes) {


            boolean isRoot = tag == tree.getRoot();

            // Do not cache pattern free queries
            // TODO It would be better to decide caching based on the actual query execution time
            // I.e. having an auto-caching layer would be nice
            boolean isPatternFree = OpUtils.isPatternFree(tag);
            if(isRoot && isPatternFree) {
                cacheWholeQuery = false;
            }

            Node serviceNode;
            if(isRoot && cacheWholeQuery) {
                serviceNode = NodeFactory.createURI("view://ex.org/view" + pop.hashCode());
                newRootServiceNode = serviceNode;
            } else {
                serviceNode = NodeFactory.createURI("view://service/" + idX++);
            }

            // We do not need to wrap parts of the query execution with a service
            // if that part is pattern free (i.e. does not depend on external data)
            // TODO Make sure that this works with EXISTS
            if(!isPatternFree) {
                Op serviceOp = new OpService(serviceNode, OpNull.create(), false);

                //TransformDisjunctionToUnion
                //tag = Transformer.transform(TransformDisjunctionToUnion.fn, tag);
                Op execOp = SparqlViewMatcherOpImpl.denormalizeOp(tag);

                // Append the prior removed projection
                //execOp = new OpProject(execOp, popVars);

                Query qq = OpAsQuery.asQuery(execOp);
                qq.getProjectVars().clear();
                qq.getProjectVars().addAll(projectVars);
                qq.setQueryResultStar(false);


                qq.setDistinct(isDistinct);

                logger.info("Root query:\n" + qq);

                RangedSupplier<Long, Binding> s3 = new RangedSupplierQuery(parentFactory::createQueryExecution, qq);

                VarInfo varInfo = new VarInfo(new LinkedHashSet<>(qq.getProjectVars()), isDistinct ? 2 : 0);
                StorageEntry se = new StorageEntry(s3, varInfo); // The var info is not used
                storageMap.put(serviceNode, se);

                taggedToService.put(tag, serviceOp);
            }
        }

        rewrittenOp = OpUtils.substitute(rewrittenOp, false, taggedToService::get);

        logger.debug("Raw query being rewritten for execution:\n" + rawQuery);
        logger.debug("Rewritten op being passed to execution:\n" + rewrittenOp);


        Context ctx = context.copy();
        ctx.put(OpExecutorViewCache.STORAGE_MAP, storageMap);


        Set<Var> visibleVars = new HashSet<>(projectVars);//OpVars.visibleVars(rewrittenOp);
        VarInfo varInfo = new VarInfo(visibleVars, isDistinct ? 2 : 0);

        RangedSupplier<Long, Binding> s2 = new RangedSupplierOp(rewrittenOp, ctx);

        if(cacheWholeQuery && newRootServiceNode != null) {
            // Caching the whole query requires the following actions:
            // (1) Allocate a new id for the query
            // (2) Create a storage entry for the rewritten entry
            // (3) Make the new id of the query together with its original (i.e. non-rewritten) op known to the rewriter

            //Node id = NodeFactory.createURI("view://ex.org/view" + queryOp.hashCode());

            s2 = new RangedSupplierLazyLoadingListCache<Binding>(executorService, s2, Range.closedOpen(0l, 10000l));
            //s2 = new RangedSupplierLazyLoadingListCache<Binding>(executorService, s2, range);

            StorageEntry se2 = new StorageEntry(s2, varInfo);

            // Update the storage entry with the cache wrapper
            //storageMap.put(newRootServiceNode, se2);

            // TODO The registration at the cache and the rewriter should be atomic
            // At least we need to deal with the chance that the rewriter maps an op to an id for
            // which the storageEntry has not yet been registered at the cache
            opRewriter.put(newRootServiceNode, pop);
            cache.put(newRootServiceNode, se2);
        }

        List<String> visibleVarNames = VarUtils.getVarNames(visibleVars);
        ResultSetCloseable result = createResultSet(visibleVarNames, s2, range, null);

        preparationTimeInSec =  sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0;

        logger.debug("Time to prepare the result set: " + (preparationTimeInSec * 1000) + " ms");


        logger.debug("CacheHitLevel:" + getCacheHitLevel());
        return result;
    }


    @Override
    protected QueryExecution executeCoreSelectX(Query query) {
        // TODO Fix bad design - this method is not needed
        return null;
    }


    public Integer getCacheHitLevel() {
        return cacheHitLevel;
    }

}





//if(false) {
//	Iterators.size(storage.apply(range));
//	@SuppressWarnings("unchecked")
//	RangedSupplierLazyLoadingListCache<Binding> test = storage.unwrap(RangedSupplierLazyLoadingListCache.class, true);
//	System.out.println("Is range cached: " + test.isCached(range));
//
//	ResultSet xxx = ResultSetUtils.create2(visibleVars, storage.apply(range));
//	Table table = TableUtils.createTable(xxx);
//	OpTable repl = OpTable.create(table);
//	rewrittenOp = repl;
//}



//StorageEntry se = new StorageEntry(storage, varInfo);
//storageMap.put(serviceNode, se);



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
//Node rootService = rewrittenOp instanceof OpService
//		? ((OpService)rewrittenOp).getService()
//	    : null;

//boolean cacheWholeQuery = true; //!rootService.getURI().startsWith("view://");

//context.put(OpExecutorViewCache.STORAGE_MAP, storageMap);



//if(cacheWholeQuery) {
//RangedSupplier<Long, Binding> s2;
//s2 = new RangedSupplierOp(rewrittenOp, ctx);



    // For each parents of which all children are in the set, remove the children from the set
    // and add the parent to the set instead




//Node serviceNode = NodeFactory.createURI("view://test.org");
//
//rewrittenOp = new OpService(serviceNode, OpNull.create(), false);
//
//RangedSupplier<Long, Binding> backend = new RangedSupplierQuery(parentFactory, rawQuery);
////RangedSupplierLazyLoadingListCache<Binding>
//RangedSupplier<Long, Binding> storage = new RangedSupplierLazyLoadingListCache<>(executorService, backend, Range.atMost(10000l), null);
//
//storage = RangedSupplierSubRange.create(storage, range);





// Adujst limit
//rewrittenOp = QueryUtils.applyRange(rewrittenOp, range);

//rewrittenOp = RewriteUtils.transformUntilNoChange(rewrittenOp, op -> Transformer.transform(TransformPushSlice.fn, op));



//DatasetGraph dg = DatasetGraphFactory.create();
//Context context = ARQ.getContext().copy();
//context.put(OpExecutorViewCache.STORAGE_MAP, storageMap);
//QueryEngineFactory qef = QueryEngineRegistry.get().find(rewrittenOp, dg, context);
//Plan plan = qef.create(rewrittenOp, dg, BindingRoot.create(), context);
//QueryIterator queryIter = plan.iterator();
//
//
////QueryIterator queryIter = x.eval(rewrittenOp, dg, BindingRoot.create(), context);
//ResultSet tmpRs = ResultSetFactory.create(queryIter, projectVarNames);
//
//// TODO Not sure if we should really return a result set, or a QueryIter instead
//ResultSetCloseable result = new ResultSetCloseable(tmpRs, () -> queryIter.close());




//ResultSetUtils.create(varNames, bindingIt)

//QueryEngineMain
//QC.execute(rewrittenOp, BindingRoot.create(), ARQ.getContext());

//org.apache.jena.query.QueryExecutionFactory.create(queryStr, syntax, model, initialBinding)
//
//
//
//LookupResult<Node> lr = viewMatcher.lookupSingle(opCache);
//RangedSupplier<Long, Binding> rangedSupplier;
//Map<Var, Var> varMap;
//if(lr == null) {
//    Node id = viewMatcher.add(opCache);
//	// Obtain the supplier from a factory (the factory may e.g. manage the sharing of a thread pool)
//
//    rangedSupplier = new RangedSupplierQuery(parentFactory, query);
//	rangedSupplier = new RangedSupplierLazyLoadingListCache<>(executorService, rangedSupplier, Range.atMost(10000l), null);
//
//	//rangedSupplier = new RangedSupplierQuery(parentFactory, q);
//	opToRangedSupplier.put(id, rangedSupplier);
//	varMap = null;
//}
//else {
//
//    varMap = Iterables.getFirst(lr.getOpVarMap().getVarMaps(), null);
//
//    assert varMap != null : "VarMap was not expected to be null at this point";
//
//	Node entryId = lr.getEntry().id;
//	rangedSupplier = opToRangedSupplier.get(entryId);
//}
//
//ResultSetCloseable result = createResultSet(rangedSupplier, range, varMap);
//return result;



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