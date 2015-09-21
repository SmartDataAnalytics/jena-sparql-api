package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDatasetGraph
    implements UpdateExecutionFactory
{
    private DatasetGraph datasetGraph;

    public UpdateExecutionFactoryDatasetGraph(DatasetGraph datasetGraph) {
        this.datasetGraph = datasetGraph;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        GraphStore graphStore = GraphStoreFactory.create(datasetGraph);
        UpdateProcessor result = com.hp.hpl.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }

}
