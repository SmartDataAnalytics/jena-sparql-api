package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

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
    public QueryExecutionFactory createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {

        if(authenticator != null && !(authenticator instanceof HttpAuthenticator)) {
            throw new RuntimeException("Authenticator is not an instance of " + HttpAuthenticator.class.getCanonicalName());
        }

        HttpAuthenticator httpAuthenticator = (HttpAuthenticator)authenticator;
        QueryExecutionFactoryHttp result = new QueryExecutionFactoryHttp(serviceUri, datasetDescription, httpAuthenticator);

        return result;
    }
}
