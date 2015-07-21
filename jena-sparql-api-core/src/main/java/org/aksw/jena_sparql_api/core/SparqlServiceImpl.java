package org.aksw.jena_sparql_api.core;


public class SparqlServiceImpl
    implements SparqlService
{
    private QueryExecutionFactory qef;
    private UpdateExecutionFactory uef;

    public SparqlServiceImpl(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        super();
        this.qef = qef;
        this.uef = uef;
    }

    @Override
    public QueryExecutionFactory getQueryExecutionFactory() {
        return qef;
    }

    @Override
    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return uef;
    }

    public static SparqlServiceImpl create(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        SparqlServiceImpl result = new SparqlServiceImpl(qef, uef);
        return result;
    }
}
