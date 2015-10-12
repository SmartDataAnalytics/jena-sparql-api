package org.aksw.jena_sparql_api.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackString;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBackString
{
    private String service;
    private DatasetDescription datasetDescription;
    private HttpAuthenticator httpAuthenticator;

    //private List<String> defaultGraphs = new ArrayList<String>();

    public QueryExecutionFactoryHttp(String service) {
        this(service, Collections.<String>emptySet());
    }

    public QueryExecutionFactoryHttp(String service, String defaultGraphName) {
        this(service, defaultGraphName == null ? Collections.<String>emptySet() : Collections.singleton(defaultGraphName));
    }

    public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
        this(service, new DatasetDescription(new ArrayList<String>(defaultGraphs), Collections.<String>emptyList()), null);
    }

    public QueryExecutionFactoryHttp(String service, DatasetDescription datasetDescription, HttpAuthenticator httpAuthenticator) {
        this.service = service;
        this.datasetDescription = datasetDescription;
        this.httpAuthenticator = httpAuthenticator;
    }

    @Override
    public String getId() {
        return service;
    }

    @Override
    public String getState() {
        String result = DatasetDescriptionUtils.toString(datasetDescription);

            //TODO Include authenticator
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryEngineHTTP qe = new QueryEngineHTTP(service, queryString, httpAuthenticator);
        qe.setDefaultGraphURIs(datasetDescription.getDefaultGraphURIs());
        qe.setNamedGraphURIs(datasetDescription.getNamedGraphURIs());


        //QueryExecution result = new QueryExecutionHttpWrapper(qe);
        QueryExecution result = qe;

        return result;
    }
}
