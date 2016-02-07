package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDataset
    extends UpdateExecutionFactoryParsingBase
{
    protected Dataset dataset;
    protected Context context;

    public UpdateExecutionFactoryDataset(Dataset dataset, Context context) {
        this.dataset = dataset;
        this.context = context;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, dataset, context);
        return result;
    }
}
