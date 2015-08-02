package org.aksw.jena_sparql_api.update;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateContext;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class SparqlServiceFactoryEventSource
    implements SparqlServiceFactory
{
    private SparqlServiceFactory delegate;
    private Set<DatasetListener> datasetListeners = new HashSet<DatasetListener>();

    public SparqlServiceFactoryEventSource(SparqlServiceFactory delegate) {
        this.delegate = delegate;
    }


    public Set<DatasetListener> getListeners() {
        return datasetListeners;
    }

    @Override
    public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {

        SparqlService core = delegate.createSparqlService(serviceUri, datasetDescription, authenticator);
        UpdateContext updateContext = new UpdateContext(core, 128, new QuadContainmentCheckerSimple()); //FunctionQuadDiffUnique.create(qef, )))
        UpdateExecutionFactoryEventSource uef = new UpdateExecutionFactoryEventSource(updateContext);

        uef.getDatasetListeners().addAll(datasetListeners);

        SparqlService result = new SparqlServiceImpl(core.getQueryExecutionFactory(), uef);

        return result;
    }
}
