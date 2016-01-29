package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import org.apache.jena.sparql.core.DatasetDescription;

public class SparqlServiceUtils {
    public static SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {

        if(authenticator != null && !(authenticator instanceof HttpAuthenticator)) {
            throw new RuntimeException("Authenticator is not an instance of " + HttpAuthenticator.class.getCanonicalName());
        }

        HttpAuthenticator httpAuthenticator = (HttpAuthenticator)authenticator;
        QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(serviceUri, datasetDescription, httpAuthenticator);
        UpdateExecutionFactoryHttp uef = new UpdateExecutionFactoryHttp(serviceUri, datasetDescription, httpAuthenticator);

        SparqlService result = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
        return result;
    }

}
