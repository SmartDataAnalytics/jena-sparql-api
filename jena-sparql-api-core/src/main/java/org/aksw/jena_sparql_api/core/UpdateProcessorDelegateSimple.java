package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;

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
    public GraphStore getGraphStore() {
        GraphStore result = delegate.getGraphStore();
        return result;
    }

    @Override
    public void execute() {
        delegate.execute();
    }
}
