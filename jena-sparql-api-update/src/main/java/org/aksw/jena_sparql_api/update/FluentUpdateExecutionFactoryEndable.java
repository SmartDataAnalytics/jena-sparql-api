package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;

/**
 * A wrapper for use with the FluentSparqlServiceFactory
 * Essentially adds a .end() method for completing configuration of the update part
 *
 * @author raven
 *
 */
public class FluentUpdateExecutionFactoryEndable
    extends FluentUpdateExecutionFactory
{
    private FluentSparqlServiceFactory fssf;

    public FluentUpdateExecutionFactoryEndable(FluentSparqlServiceFactory fssf) {
        super(fssf.sparqlService.getUpdateExecutionFactory());
    }

    /**
     * A user should never call .create() here
     */
    @Override
    public UpdateExecutionFactory create() {
        throw new RuntimeException("Do not call create() - call .end().create() instead");
    }

//    /**
//     * But internally, we need access to .create()
//     *
//     * @return
//     */
//    public UpdateExecutionFactory retrieve() {
//        UpdateExecutionFactory result = super.create();
//        return result;
//    }

    public FluentSparqlServiceFactory end() {
        QueryExecutionFactory qef= fssf.sparqlService.getQueryExecutionFactory();
        UpdateExecutionFactory uef = super.create();

        fssf.sparqlService = new SparqlServiceImpl(qef, uef);
        return fssf;
    }
}

