package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;

public class FluentQueryExecutionFactoryEndable
    extends FluentQueryExecutionFactory
{
    private FluentSparqlServiceFactory fssf;

    public FluentQueryExecutionFactoryEndable(FluentSparqlServiceFactory fssf) {
        super(fssf.sparqlService.getQueryExecutionFactory());
    }

    // If a user calls .create() its most likely an error
    @Override
    public QueryExecutionFactory create() {
        throw new RuntimeException("Do not call create() - call .end().create() instead");
    }

//    // But internally, we need access to create
//    public QueryExecutionFactory retrieve() {
//        QueryExecutionFactory result = super.create();
//        return result;
//    }

    public FluentSparqlServiceFactory end() {
        QueryExecutionFactory qef = super.create();
        UpdateExecutionFactory uef= fssf.sparqlService.getUpdateExecutionFactory();

        fssf.sparqlService = new SparqlServiceImpl(qef, uef);
        return fssf;
    }
}

