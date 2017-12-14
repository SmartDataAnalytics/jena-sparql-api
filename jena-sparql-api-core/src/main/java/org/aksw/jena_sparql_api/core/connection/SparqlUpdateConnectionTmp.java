package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public interface SparqlUpdateConnectionTmp
    extends TransactionalTmp, SparqlUpdateConnection
{
    // ---- SparqlUpdateConnection

    /** Execute a SPARQL Update.
     *
     * @param update
     */
    @Override
    public default void update(Update update) {
        update(new UpdateRequest(update));
    }


    /** Execute a SPARQL Update.
     *
     * @param updateString
     */
    @Override
     default void update(String updateString) {
        update(UpdateFactory.create(updateString));
    }
}
