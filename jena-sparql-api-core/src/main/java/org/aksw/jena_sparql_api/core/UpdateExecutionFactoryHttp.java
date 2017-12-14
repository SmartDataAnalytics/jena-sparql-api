package org.aksw.jena_sparql_api.core;

import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryHttp
    extends UpdateExecutionFactoryParsingBase
{
    private String remoteEndpoint;
    //private HttpAuthenticator authenticator;
    private HttpClient httpClient;
    private DatasetDescription datasetDescription;

    public UpdateExecutionFactoryHttp(String remoteEndpoint) {
        this(remoteEndpoint, null);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, HttpClient httpClient) {
        this(remoteEndpoint, new DatasetDescription(), httpClient);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, DatasetDescription datasetDescription, HttpClient httpClient) {
        this.remoteEndpoint = remoteEndpoint;
        this.datasetDescription = datasetDescription;
        this.httpClient = httpClient;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        // Fixing var names should be done with transform
        // UpdateRequestUtils.fixVarNames(updateRequest);

        UpdateProcessRemote result = new UpdateProcessRemote(updateRequest, remoteEndpoint, null);//request, endpoint, context);

        result.setClient(httpClient);
        result.setDefaultGraphs(datasetDescription.getDefaultGraphURIs());;
        result.setNamedGraphs(datasetDescription.getNamedGraphURIs());

        return result;
    }
}
