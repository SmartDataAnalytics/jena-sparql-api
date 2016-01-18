package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

@Deprecated // Use UpdateExecutionFactoryGraphStore instead
public class UpdateExecutionFactoryDataset
    extends UpdateExecutionFactoryParsingBase
{
    private Dataset dataset;

    public UpdateExecutionFactoryDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        GraphStore graphStore = GraphStoreFactory.create(dataset);
        UpdateProcessor result = com.hp.hpl.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }
}
