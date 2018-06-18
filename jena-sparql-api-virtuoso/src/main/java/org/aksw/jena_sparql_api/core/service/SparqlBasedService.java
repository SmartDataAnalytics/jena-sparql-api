package org.aksw.jena_sparql_api.core.service;

import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.util.concurrent.Service;

/**
 * Candidate for deprecation.
 * Used to obtain RDF connections from docker services; but instead one
 * could just connect to the container host name
 * RDFConnectionFactory.connect(service.getContainerId());
 * However, this would not work for in-memory stores...
 * On the other hand, the life cycle of the store should be managed
 * separated from the connection anyway.
 * 
 * @author raven
 *
 */
public interface SparqlBasedService
    extends Service
{
    RDFConnection createDefaultConnection();
}
