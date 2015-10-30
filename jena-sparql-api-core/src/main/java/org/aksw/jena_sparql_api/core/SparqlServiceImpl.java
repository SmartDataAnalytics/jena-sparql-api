package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class SparqlServiceImpl
    implements SparqlService
{
    private DatasetDescription datasetDescription;
    private QueryExecutionFactory qef;
    private UpdateExecutionFactory uef;

    public SparqlServiceImpl(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        this(null, qef, uef);
    }

    public SparqlServiceImpl(DatasetDescription datasetDescription, QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        super();
        this.datasetDescription = datasetDescription;
        this.qef = qef;
        this.uef = uef;
    }

    public DatasetDescription getDatasetDescription() {
        return datasetDescription;
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
