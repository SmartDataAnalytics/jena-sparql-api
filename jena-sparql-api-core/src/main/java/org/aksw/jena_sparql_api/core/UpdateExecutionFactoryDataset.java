package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

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
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }
}
