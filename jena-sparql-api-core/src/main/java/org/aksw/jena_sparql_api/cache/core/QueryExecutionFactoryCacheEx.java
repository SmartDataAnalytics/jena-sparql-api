package org.aksw.jena_sparql_api.cache.core;

import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:08 PM
 */
public class QueryExecutionFactoryCacheEx
        extends QueryExecutionFactoryDecorator
{
    private CacheEx cache;
    private String service;

    public QueryExecutionFactoryCacheEx(QueryExecutionFactory decoratee, CacheEx cache) {
        this(decoratee, decoratee.getId(), cache);
    }

    public QueryExecutionFactoryCacheEx(QueryExecutionFactory decoratee, String service, CacheEx cache) {
        super(decoratee);
        this.service = service;
        this.cache = cache;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return new QueryExecutionCacheEx(super.createQueryExecution(query), service, query.toString(), cache);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return new QueryExecutionCacheEx(super.createQueryExecution(queryString), service, queryString, cache);
    }
}
