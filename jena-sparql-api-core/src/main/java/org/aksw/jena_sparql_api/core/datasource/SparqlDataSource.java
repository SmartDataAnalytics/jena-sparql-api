package org.aksw.jena_sparql_api.core.datasource;

import org.apache.jena.rdfconnection.RDFConnection;

/** An connection factory interface; useful in conjunction with spring dependency injection */
public interface SparqlDataSource {
    RDFConnection newConnection();
}
