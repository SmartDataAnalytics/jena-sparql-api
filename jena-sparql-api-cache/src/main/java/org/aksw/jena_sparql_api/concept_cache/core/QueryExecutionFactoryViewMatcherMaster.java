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
    protected OpRewriteViewMatcherStateful viewMatcher;
    protected ExecutorService executorService;

    protected long indexResultSetSizeThreshold;
    protected Map<Node, ? super ViewCacheIndexer> serviceMap;
    protected boolean closeExecutorService;

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, OpRewriteViewMatcherStateful viewMatcher, ExecutorService executorService, boolean closeExecutorService) { //long indexResultSetSizeThreshold) {
        super(decoratee);
        this.viewMatcher = viewMatcher;
        this.executorService = executorService;
        this.closeExecutorService = closeExecutorService;
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
            ExecutorService executorService,
            boolean closeExecutorService
            ) {

        OpRewriteViewMatcherStateful viewMatcherRewriter = new OpRewriteViewMatcherStateful(queryCache, removalListeners);
        QueryExecutionFactoryViewMatcherMaster result = new QueryExecutionFactoryViewMatcherMaster(qef, viewMatcherRewriter, executorService, closeExecutorService);

        return result;
    }


    public static QueryExecutionFactoryViewMatcherMaster create(QueryExecutionFactory qef, CacheBuilder<Object, Object> cacheBuilder, ExecutorService executorService, boolean closeExecutorService) {
        RemovalListenerMultiplexer<Node, StorageEntry> removalListeners = new RemovalListenerMultiplexer<>();

        Cache<Node, StorageEntry> queryCache = cacheBuilder
                .removalListener(removalListeners)
                .build();

        QueryExecutionFactoryViewMatcherMaster result = create(qef, queryCache, removalListeners.getClients(), executorService, closeExecutorService);

        return result;
    }

    @Override
    public void close() throws Exception {
        if(closeExecutorService) {
            executorService.shutdown();
        }
        super.close();
    }

}
