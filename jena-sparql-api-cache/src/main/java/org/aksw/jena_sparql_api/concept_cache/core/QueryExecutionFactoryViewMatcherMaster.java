package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.cache.RemovalListenerMultiplexer;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;


public class QueryExecutionFactoryViewMatcherMaster
    extends QueryExecutionFactoryDecorator
{
    //protected SparqlViewMatcherSystem viewMatcherSystem;
	//protected QueryExecutionFactory decoratee;
	protected OpRewriteViewMatcherStateful viewMatcher;
	protected ExecutorService executorService;
	//protected Map<Node, RangedSupplier<Long, Binding>> opToRangedSupplier;
	//protected Map<Node, StorageEntry> storageMap;

    protected long indexResultSetSizeThreshold;

    protected Map<Node, ? super ViewCacheIndexer> serviceMap;


//    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap) {
//        this(decoratee, serviceMap, new SparqlViewMatcherSystemImpl(), 10000);
//    }

//    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpRewriteViewMatcherStateful viewMatcher) { //long indexResultSetSizeThreshold) {
//        super(decoratee);
//        this.viewMatcher = viewMatcher;
//    }

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpRewriteViewMatcherStateful viewMatcher, ExecutorService executorService) { //long indexResultSetSizeThreshold) {
        super(decoratee);
        //this.viewMatcherSystem = viewMatcherSystem;
    	//this.decoratee = decoratee;
        this.viewMatcher = viewMatcher;
        this.executorService = executorService;
        //this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        //this.storageMap = storageMap;
        //this.opToRangedSupplier = new HashMap<>();
    }

    public Cache<Node, StorageEntry> getCache() {
    	Cache<Node, StorageEntry> result = viewMatcher.getCache();
    	return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
    	QueryExecution result = new QueryExecutionViewMatcherMaster(query, decoratee, viewMatcher, executorService);//, viewMatcher, executorService, storageMap);
    	return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
    	throw new RuntimeException("parsing required");
    }

    public static QueryExecutionFactoryViewMatcherMaster create(
    		QueryExecutionFactory qef,
    		Cache<Node, StorageEntry> queryCache,
    		Collection<RemovalListener<Node, StorageEntry>> removalListeners,
    		ExecutorService executorService) {

		OpRewriteViewMatcherStateful viewMatcherRewriter = new OpRewriteViewMatcherStateful(queryCache, removalListeners);
		QueryExecutionFactoryViewMatcherMaster result = new QueryExecutionFactoryViewMatcherMaster(qef, viewMatcherRewriter, executorService);

        return result;
    }


    public static QueryExecutionFactoryViewMatcherMaster create(QueryExecutionFactory qef, CacheBuilder<Object, Object> cacheBuilder, ExecutorService executorService) {
		RemovalListenerMultiplexer<Node, StorageEntry> removalListeners = new RemovalListenerMultiplexer<>();

		Cache<Node, StorageEntry> queryCache = cacheBuilder
				.removalListener(removalListeners)
				.build();

		QueryExecutionFactoryViewMatcherMaster result = create(qef, queryCache, removalListeners.getClients(), executorService);

        return result;
    }

}
