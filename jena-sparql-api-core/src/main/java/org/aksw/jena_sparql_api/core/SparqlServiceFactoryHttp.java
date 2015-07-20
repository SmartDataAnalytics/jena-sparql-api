package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

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
    public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {
        SparqlService result = SparqlServiceUtils.createSparqlService(serviceUri, datasetDescription, authenticator);
        return result;
    }
}
