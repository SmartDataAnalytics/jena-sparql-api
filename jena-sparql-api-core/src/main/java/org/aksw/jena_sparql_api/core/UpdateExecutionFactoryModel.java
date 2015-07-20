package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryModel
    implements UpdateExecutionFactory
{
    private Model model;

    public UpdateExecutionFactoryModel(Model model) {
        this.model = model;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        GraphStore graphStore = GraphStoreFactory.create(model);
        UpdateProcessor result = com.hp.hpl.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }

}
