package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpcImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;

// https://jena.apache.org/documentation/query/arq-query-eval.html
/**
 * Use QueryExecutionFactoryViewMatcherMaster instead.
 * 
 * QueryExecutionFactory that is capable of caching (fragments of) SPARQL queries.
 *
 *
 * @author raven
 *
 */
@Deprecated
public class QueryExecutionFactoryViewCacheMaster
    extends QueryExecutionFactoryDecorator
{
    protected SparqlViewMatcherQfpc conceptMap;
    protected long indexResultSetSizeThreshold;

    protected Map<Node, ? super ViewCacheIndexer> serviceMap;


    public QueryExecutionFactoryViewCacheMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap) {
        this(decoratee, serviceMap, new SparqlViewMatcherQfpcImpl(), 10000);
    }

    public QueryExecutionFactoryViewCacheMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap, SparqlViewMatcherQfpc conceptMap, long indexResultSetSizeThreshold) {
        super(decoratee);
        this.conceptMap = conceptMap;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        this.serviceMap = serviceMap;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        Query rawQuery = QueryFactory.create(queryString);
        QueryExecution result = createQueryExecution(rawQuery);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = SparqlCacheUtils.prepareQueryExecution(decoratee, serviceMap, query, conceptMap, indexResultSetSizeThreshold);
        return result;
    }

}