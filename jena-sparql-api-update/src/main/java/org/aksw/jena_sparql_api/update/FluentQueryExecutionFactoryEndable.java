package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;

public class FluentQueryExecutionFactoryEndable
    extends FluentQueryExecutionFactory
{
    private FluentSparqlService fssf;

    public FluentQueryExecutionFactoryEndable(FluentSparqlService fssf) {
        super(fssf.sparqlService.getQueryExecutionFactory());
        this.fssf = fssf;
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

    public FluentSparqlService end() {
        QueryExecutionFactory qef = super.create();
        UpdateExecutionFactory uef= fssf.sparqlService.getUpdateExecutionFactory();

        fssf.sparqlService = new SparqlServiceImpl(qef, uef);
        return fssf;
    }
}

