package org.aksw.jena_sparql_api.core;

import org.apache.jena.update.GraphStore;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * Use UpdateExecutionFactoryDataset instead
 *
 * @author raven
 *
 */
@Deprecated
public class UpdateExecutionFactoryGraphStore
        extends UpdateExecutionFactoryParsingBase
{

    protected GraphStore graphStore;

    public UpdateExecutionFactoryGraphStore(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, graphStore);
        return result;
    }
}


