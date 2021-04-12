package org.aksw.jena_sparql_api.core;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDatasetGraph
    extends UpdateExecutionFactoryParsingBase
{
    private DatasetGraph datasetGraph;

    public UpdateExecutionFactoryDatasetGraph(DatasetGraph datasetGraph) {
        this.datasetGraph = datasetGraph;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, datasetGraph);
        return result;
    }

}
