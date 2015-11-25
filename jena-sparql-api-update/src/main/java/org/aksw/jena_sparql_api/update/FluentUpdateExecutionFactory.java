package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryHttp;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryModel;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class FluentUpdateExecutionFactory {
    private UpdateExecutionFactory uef;

    public FluentUpdateExecutionFactory(UpdateExecutionFactory uef) {
        super();
        this.uef = uef;
    }

    public UpdateExecutionFactory create() {
        return uef;
    }


    public static FluentUpdateExecutionFactory from(UpdateExecutionFactory uef) {
        FluentUpdateExecutionFactory result = new FluentUpdateExecutionFactory(uef);
        return result;
    }

    public static FluentUpdateExecutionFactory from(Model model) {
        UpdateExecutionFactory uef = new UpdateExecutionFactoryModel(model);

        FluentUpdateExecutionFactory result = FluentUpdateExecutionFactory.from(uef);
        return result;
    }

    public static FluentUpdateExecutionFactory http(String endpointUrl, DatasetDescription datasetDescription, HttpAuthenticator authenticator) {
        UpdateExecutionFactory uef = new UpdateExecutionFactoryHttp(endpointUrl, datasetDescription, authenticator);

        FluentUpdateExecutionFactory result = FluentUpdateExecutionFactory.from(uef);
        return result;
    }

}
