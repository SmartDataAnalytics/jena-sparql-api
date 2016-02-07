package org.aksw.jena_sparql_api.core;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

//@Deprecated // Use UpdateExecutionFactoryGraphStore instead
public class UpdateExecutionFactoryDatasetGraph
    extends UpdateExecutionFactoryParsingBase
{
    private DatasetGraph datasetGraph;

    public UpdateExecutionFactoryDatasetGraph(DatasetGraph datasetGraph) {
        this.datasetGraph = datasetGraph;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        GraphStore graphStore = GraphStoreFactory.create(datasetGraph);
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }

}
