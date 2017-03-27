package org.aksw.jena_sparql_api.core;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

@Deprecated // Use UpdateExecutionFactoryGraphStore instead
public class UpdateExecutionFactoryModel
    extends UpdateExecutionFactoryParsingBase
{
    private Model model;

    public UpdateExecutionFactoryModel(Model model) {
        this.model = model;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        GraphStore graphStore = GraphStoreFactory.create(model);
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }

}
