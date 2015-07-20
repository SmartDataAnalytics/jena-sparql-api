package org.aksw.jena_sparql_api.update;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.SparqlService;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateProcessorEventSource
    implements UpdateProcessor
{
    private UpdateExecutionFactoryEventSource factory;
    private UpdateRequest updateRequest;

    /**
     * Listeners only for this individual update request
     */
    private Set<DatasetListener> listeners = new HashSet<DatasetListener>();

    public UpdateProcessorEventSource(UpdateExecutionFactoryEventSource factory, UpdateRequest updateRequest) {
        this.factory = factory;
        this.updateRequest = updateRequest;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public GraphStore getGraphStore() {
        return null;
    }

    @Override
    public void execute() {
        Iterable<DatasetListener> allListeners = Iterables.concat(listeners, factory.getListeners());

        UpdateContext context = factory.getContext();


        //UpdateExecutionFactory uef = context.getUpdateExecutionFactory();
        //QueryExecutionFactory qef = context.getQueryExecutionFactory();
        SparqlService sparqlService = context.getSparqlService();
        int batchSize = context.getBatchSize();
        QuadContainmentChecker containmentChecker = context.getContainmentChecker();
        UpdateUtils.executeUpdate(sparqlService, updateRequest, batchSize, allListeners, containmentChecker);
    }

}
