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
    protected UpdateProcessorFactory updateProcessorFactory;

    @FunctionalInterface
    public static interface UpdateProcessorFactory {
    	UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, Context context);
    }
    
    public UpdateExecutionFactoryDataset(Dataset dataset) {
        this(dataset, null);
    }

    public UpdateExecutionFactoryDataset(Dataset dataset, Context context) {
        this(dataset, context, org.apache.jena.update.UpdateExecutionFactory::create);
    }

    public UpdateExecutionFactoryDataset(Dataset dataset, Context context, UpdateProcessorFactory updateProcessorFactory) {
    	super();
    	this.dataset = dataset;
        this.context = context;
        this.updateProcessorFactory = updateProcessorFactory;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        UpdateProcessor result = updateProcessorFactory.create(updateRequest, dataset, context);
        return result;
    }
}
