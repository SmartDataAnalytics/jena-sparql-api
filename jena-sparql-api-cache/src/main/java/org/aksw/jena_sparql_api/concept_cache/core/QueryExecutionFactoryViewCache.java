package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.concept_cache.dirty.ConceptMap;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;

// https://jena.apache.org/documentation/query/arq-query-eval.html
public class QueryExecutionFactoryViewCache
    extends QueryExecutionFactoryDecorator
{
    protected ConceptMap conceptMap;
    protected long indexResultSetSizeThreshold;

    public QueryExecutionFactoryViewCache(QueryExecutionFactory decoratee) {
        this(decoratee, new ConceptMap(), 10000);
    }

    public QueryExecutionFactoryViewCache(QueryExecutionFactory decoratee, ConceptMap conceptMap, long indexResultSetSizeThreshold) {
        super(decoratee);
        this.conceptMap = conceptMap;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        Query rawQuery = QueryFactory.create(queryString);
        QueryExecution result = createQueryExecution(rawQuery);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = SparqlCacheUtils.prepareQueryExecution(decoratee, query, conceptMap, indexResultSetSizeThreshold);
        return result;
    }

}