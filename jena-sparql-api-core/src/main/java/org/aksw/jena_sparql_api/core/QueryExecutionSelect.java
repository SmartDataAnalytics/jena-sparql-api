package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionSelect
    extends QueryExecutionBaseSelect
{
    private QueryExecutionFactory subFactory;

    public QueryExecutionSelect(QueryExecutionFactory parentFactory, Query query, QueryExecutionFactory subFactory) {
        super(query, parentFactory);
        this.subFactory = subFactory;
    }

    @Override
    protected QueryExecution executeCoreSelectX(Query query) {
        QueryExecution result = this.subFactory.createQueryExecution(query);
        return result;
    }
}
