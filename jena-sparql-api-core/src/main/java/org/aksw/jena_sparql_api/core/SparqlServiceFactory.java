package org.aksw.jena_sparql_api.core;

import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * Interface for creating QueryExecutionFactories, based on service and default graph URIs.
 *
 * @author raven
 *
 */
public interface SparqlServiceFactory {
    SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient);
}
