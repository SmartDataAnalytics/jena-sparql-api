package org.aksw.jena_sparql_api.core;

import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * TODO Find a concept where we create an initial qef, and then support wrapping it
 *
 * @author raven
 *
 */
public class SparqlServiceFactoryHttp
    implements SparqlServiceFactory
{
    public SparqlServiceFactoryHttp() {
    }

    @Override
    public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, HttpClient httpClient) {
        SparqlService result = SparqlServiceUtils.createSparqlService(serviceUri, datasetDescription, httpClient);
        return result;
    }
}
