package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.modify.UpdateProcessRemote;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryHttp
    extends UpdateExecutionFactoryParsingBase
{
    private String remoteEndpoint;
    private HttpAuthenticator authenticator;
    private DatasetDescription datasetDescription;

    public UpdateExecutionFactoryHttp(String remoteEndpoint) {
        this(remoteEndpoint, null);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, HttpAuthenticator authenticator) {
        this(remoteEndpoint, new DatasetDescription(), authenticator);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, DatasetDescription datasetDescription, HttpAuthenticator authenticator) {
        this.remoteEndpoint = remoteEndpoint;
        this.datasetDescription = datasetDescription;
        this.authenticator = authenticator;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateRequestUtils.fixVarNames(updateRequest);

        UpdateProcessRemote result = new UpdateProcessRemote(updateRequest, remoteEndpoint, null);//request, endpoint, context);

        result.setAuthenticator(authenticator);
        result.setDefaultGraphs(datasetDescription.getDefaultGraphURIs());;
        result.setNamedGraphs(datasetDescription.getNamedGraphURIs());

        return result;
    }
}
