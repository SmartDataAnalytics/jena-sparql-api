package org.aksw.jena_sparql_api.update;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryHttp
    implements UpdateExecutionFactory
{
    private String remoteEndpoint;
    private HttpAuthenticator authenticator;

    public UpdateExecutionFactoryHttp(String remoteEndpoint) {
        this(remoteEndpoint, null);
    }

    public UpdateExecutionFactoryHttp(String remoteEndpoint, HttpAuthenticator authenticator) {
        this.remoteEndpoint = remoteEndpoint;
        this.authenticator = authenticator;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = com.hp.hpl.jena.update.UpdateExecutionFactory.createRemote(updateRequest, remoteEndpoint, authenticator);
        return result;
    }
}
