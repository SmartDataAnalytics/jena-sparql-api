package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Syntax;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public abstract class UpdateExecutionFactoryParsingBase
    implements UpdateExecutionFactory
{
    @Override
    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestStr, Syntax.syntaxARQ);
        UpdateProcessor result = this.createUpdateProcessor(updateRequest);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
    }

    @Override
    public void close() throws Exception {
    }
}
