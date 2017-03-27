package org.aksw.jena_sparql_api.core;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryPrefix
    extends UpdateExecutionFactoryParsingBase
{
    private UpdateExecutionFactory delegate;
    private PrefixMapping prefixMapping;

    public UpdateExecutionFactoryPrefix(UpdateExecutionFactory delegate, PrefixMapping prefixMapping) {
        super();
        this.delegate = delegate;
        this.prefixMapping = prefixMapping;
    }

    public UpdateExecutionFactory getDelegate() {
        return delegate;
    }

    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        // TODO We should clone the request first
        updateRequest.getPrefixMapping().setNsPrefixes(prefixMapping);

        UpdateProcessor result = delegate.createUpdateProcessor(updateRequest);
        return result;
    }
}
