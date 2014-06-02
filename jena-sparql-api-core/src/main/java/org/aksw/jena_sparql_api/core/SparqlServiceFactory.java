package org.aksw.jena_sparql_api.core;

import java.util.Collection;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 * 
 * @author raven
 *
 */
public interface SparqlServiceFactory {
    QueryExecutionFactory createSparqlService(String serviceUri, Collection<String> defaultGraphUris);
}
