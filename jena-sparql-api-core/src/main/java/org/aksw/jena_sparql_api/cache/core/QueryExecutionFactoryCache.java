package org.aksw.jena_sparql_api.cache.core;

import org.aksw.jena_sparql_api.cache.extra.Cache;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.query.Query;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:08 PM
 */
public class QueryExecutionFactoryCache
        extends QueryExecutionFactoryDecorator
{
    private Cache cache;

    public QueryExecutionFactoryCache(QueryExecutionFactory decoratee, Cache cache) {
        super(decoratee);
        this.cache = cache;
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
        return new QueryExecutionCache(super.createQueryExecution(query), query.toString(), cache);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
        return new QueryExecutionCache(super.createQueryExecution(queryString), queryString, cache);
    }
}
