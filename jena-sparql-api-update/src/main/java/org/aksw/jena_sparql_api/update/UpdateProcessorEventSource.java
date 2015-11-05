package org.aksw.jena_sparql_api.update;

import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteInsert;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateProcessorEventSource
    implements UpdateProcessor
{
    protected UpdateExecutionFactoryEventSource factory;
    protected UpdateRequest updateRequest;
    //protected SparqlServiceReference ssr;

    /**
     * Listeners only for this individual update request
     */
    protected Set<DatasetListener> listeners = new HashSet<DatasetListener>();

    public UpdateProcessorEventSource(UpdateExecutionFactoryEventSource factory, UpdateRequest updateRequest) {
        this.factory = factory;
        this.updateRequest = updateRequest;
        //this.ssr = ssr;
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
        Iterable<DatasetListener> allListeners = Iterables.concat(listeners, factory.getDatasetListeners());

        UpdateContext context = factory.getContext();


        //UpdateExecutionFactory uef = context.getUpdateExecutionFactory();
        //QueryExecutionFactory qef = context.getQueryExecutionFactory();
        SparqlService sparqlService = context.getSparqlService();
        int batchSize = context.getBatchSize();
        QuadContainmentChecker containmentChecker = context.getContainmentChecker();
        UpdateExecutionUtils.executeUpdate(sparqlService, updateRequest, batchSize, containmentChecker, allListeners);
    }

}
