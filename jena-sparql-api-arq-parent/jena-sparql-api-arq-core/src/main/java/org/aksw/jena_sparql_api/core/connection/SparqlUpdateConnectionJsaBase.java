package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.arq.core.update.UpdateProcessorFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionJsaBase<T extends UpdateProcessorFactory>
    implements SparqlUpdateConnectionTmp
{
    protected T updateProcessorFactory;
    protected Transactional transactional;

    public SparqlUpdateConnectionJsaBase(T updateProcessorFactory) {
        this(updateProcessorFactory, new TransactionalTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }});
    }

    public SparqlUpdateConnectionJsaBase(T updateProcessorFactory, Transactional transactional) {
        super();
        this.updateProcessorFactory = updateProcessorFactory;
        this.transactional = transactional;
    }

    @Override
    public void update(UpdateRequest updateRequest) {
        UpdateProcessor updateProcessor = updateProcessorFactory.createUpdateProcessor(updateRequest);
        updateProcessor.execute();
    }

    @Override
    public void close() {
    }

    @Override
    public Transactional getDelegate() {
        return transactional;
    }
}
