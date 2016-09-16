package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystem;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystemImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactoryViewMatcherMaster
    extends QueryExecutionFactoryDecorator
{
    protected SparqlViewMatcherSystem viewMatcherSystem;
    protected long indexResultSetSizeThreshold;

    protected Map<Node, ? super ViewCacheIndexer> serviceMap;


    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap) {
        this(decoratee, serviceMap, new SparqlViewMatcherSystemImpl(), 10000);
    }

    public QueryExecutionFactoryViewMatcherMaster(QueryExecutionFactory decoratee, Map<Node, ? super ViewCacheIndexer> serviceMap, SparqlViewMatcherSystem viewMatcherSystem, long indexResultSetSizeThreshold) {
        super(decoratee);
        this.viewMatcherSystem = viewMatcherSystem;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        this.serviceMap = serviceMap;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = SparqlCacheUtils.prepareQueryExecution(decoratee, serviceMap, query, viewMatcherSystem, indexResultSetSizeThreshold);
        return result;
    }

}
