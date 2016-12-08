package org.aksw.jena_sparql_api.core;

import org.apache.jena.sparql.core.DatasetDescription;

public class SparqlServiceImpl
    implements SparqlService
{
    protected String serviceUri;
    protected DatasetDescription datasetDescription;
    protected QueryExecutionFactory qef;
    protected UpdateExecutionFactory uef;

    public SparqlServiceImpl(QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        this(null, null, qef, uef);
    }

    public SparqlServiceImpl(String serviceUri, DatasetDescription datasetDescription, QueryExecutionFactory qef, UpdateExecutionFactory uef) {
        super();
        this.serviceUri = serviceUri;
        this.datasetDescription = datasetDescription;
        this.qef = qef;
        this.uef = uef;
    }

    @Override
    public String getServiceUri() {
        return serviceUri;
    }

    @Override
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

//    @Override
//    public void close() throws Exception {
//        qef.close();
//        //uef.close();
//    }
}
