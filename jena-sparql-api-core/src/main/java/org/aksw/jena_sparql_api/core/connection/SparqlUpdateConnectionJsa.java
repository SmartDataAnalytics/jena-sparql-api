package org.aksw.jena_sparql_api.core.connection;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.sparql.core.Transactional;

public class SparqlUpdateConnectionJsa
   extends SparqlUpdateConnectionJsaBase<UpdateExecutionFactory>
{
    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory) {
        this(updateExecutionFactory, new TransactionalTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }});
    }

    public SparqlUpdateConnectionJsa(UpdateExecutionFactory updateExecutionFactory, Transactional transactional) {
        super(updateExecutionFactory, transactional);
    }

    @Override
    public void close() {
        try {
            updateProcessorFactory.close();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
