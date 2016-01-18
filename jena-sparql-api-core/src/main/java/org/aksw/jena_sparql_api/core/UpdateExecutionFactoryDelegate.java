package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDelegate
    extends UpdateExecutionFactoryParsingBase
{
    protected UpdateExecutionFactory delegate;

    public UpdateExecutionFactoryDelegate(UpdateExecutionFactory delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = delegate.createUpdateProcessor(updateRequest);
        return result;
    }
}
