package org.aksw.jena_sparql_api.core.connection;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class SparqlUpdateConnectionMultiplex
    extends TransactionalMultiplex<SparqlUpdateConnection>
    implements SparqlUpdateConnection
{

    public SparqlUpdateConnectionMultiplex(SparqlUpdateConnection ... delegates) {
        this(Arrays.asList(delegates));
    }

    public SparqlUpdateConnectionMultiplex(Collection<? extends SparqlUpdateConnection> delegates) {
        super(delegates);
    }

    @Override
    public void update(Update update) {
        TransactionalMultiplex.forEach(delegates, d -> d.update(update));
    }

    @Override
    public void update(UpdateRequest update) {
        TransactionalMultiplex.forEach(delegates, d -> d.update(update));
    }

    @Override
    public void update(String updateString) {
        TransactionalMultiplex.forEach(delegates, d -> d.update(updateString));
    }

    @Override
    public void close() {
        TransactionalMultiplex.forEach(delegates, d -> d.close());
    }


}
