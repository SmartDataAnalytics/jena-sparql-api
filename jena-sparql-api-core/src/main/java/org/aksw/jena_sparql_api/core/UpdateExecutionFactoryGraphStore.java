package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryGraphStore
        extends UpdateExecutionFactoryParsingBase
{

    protected GraphStore graphStore;

    public UpdateExecutionFactoryGraphStore(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = com.hp.hpl.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }
}


