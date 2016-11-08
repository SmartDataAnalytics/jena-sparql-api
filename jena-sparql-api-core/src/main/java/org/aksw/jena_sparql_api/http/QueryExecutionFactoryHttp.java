package org.aksw.jena_sparql_api.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBase;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBase
{
    private String service;
    private DatasetDescription datasetDescription;
    private HttpClient httpClient;

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

    public QueryExecutionFactoryHttp(String service, DatasetDescription datasetDescription, HttpClient httpClient) {
        this.service = service;
        this.datasetDescription = datasetDescription;
        this.httpClient = httpClient;
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

    public QueryExecution postProcesss(QueryEngineHTTP qe) {
        qe.setDefaultGraphURIs(datasetDescription.getDefaultGraphURIs());
        qe.setNamedGraphURIs(datasetDescription.getNamedGraphURIs());

        QueryExecution result = new QueryExecutionHttpWrapper(qe);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryEngineHTTP qe = new QueryEngineHTTP(service, queryString, httpClient);
        QueryExecution result = postProcesss(qe);

        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryEngineHTTP qe = new QueryEngineHTTP(service, query, httpClient);
        QueryExecution result = postProcesss(qe);

        return result;
    }

}
