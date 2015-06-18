package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 *
 * @author raven
 *
 */
public interface SparqlServiceFactory {
    QueryExecutionFactory createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator);
}
