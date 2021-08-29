package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionJsa
    implements SparqlUpdateConnectionTmp
{
    protected UpdateExecutionFactory updateExecutionFactory;
    protected Transactional transactional;

    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory) {
        this(updateExecutionFactory, new TransactionalTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }});
    }

    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory, Transactional transactional) {
        super();
        this.updateExecutionFactory = updateExecutionFactory;
        this.transactional = transactional;
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

    @Override
    public Transactional getDelegate() {
        return transactional;
    }
}
