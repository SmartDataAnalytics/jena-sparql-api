package org.aksw.jena_sparql_api.core;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public abstract class UpdateProcessorDelegateSimple
    implements UpdateProcessor
{
    protected UpdateProcessor delegate;

    public UpdateProcessorDelegateSimple(UpdateProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public Context getContext() {
        Context result = delegate.getContext();
        return result;
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        DatasetGraph result = delegate.getDatasetGraph();
        return result;
    }

    @Override
    public void execute() {
        delegate.execute();
    }
}
