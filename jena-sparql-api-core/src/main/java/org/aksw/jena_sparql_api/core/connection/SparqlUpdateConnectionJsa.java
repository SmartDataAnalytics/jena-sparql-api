package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionJsa
    implements SparqlUpdateConnectionTmp
{
    protected UpdateExecutionFactory updateExecutionFactory;

    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory) {
        super();
        this.updateExecutionFactory = updateExecutionFactory;
    }

    @Override
    public void update(UpdateRequest updateRequest) {
        UpdateProcessor updateProcessor = updateExecutionFactory.createUpdateProcessor(updateRequest);
        updateProcessor.execute();
    }

    @Override
    public void close() {
        try {
            updateExecutionFactory.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
