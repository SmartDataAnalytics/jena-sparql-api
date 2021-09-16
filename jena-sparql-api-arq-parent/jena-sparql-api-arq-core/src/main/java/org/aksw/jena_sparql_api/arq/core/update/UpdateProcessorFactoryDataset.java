package org.aksw.jena_sparql_api.arq.core.update;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.modify.UpdateProcessorBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateProcessorFactoryDataset
    implements UpdateProcessorFactory
{
    protected Dataset dataset;
    protected Context context;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider;


    public UpdateProcessorFactoryDataset(Dataset dataset) {
        this(dataset, null);
    }

    public UpdateProcessorFactoryDataset(Dataset dataset, Context context) {
        this(dataset, context, UpdateEngineRegistry.get()::find);
    }

    public UpdateProcessorFactoryDataset(Dataset dataset, Context context, UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        super();
        this.dataset = dataset;
        this.context = context;
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext.
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        UpdateEngineFactory f = updateEngineFactoryProvider.find(dsg, context);
        if ( f == null )
        {
            Log.warn(UpdateProcessorFactoryDataset.class, "Failed to find a UpdateEngineFactory for update: " + updateRequest) ;
            return null ;
        }
        //dataset.begin(ReadWrite.WRITE);
    //    QueryExecutionBase tmp = new QueryExecutionBase(query, dataset, context, f) ;
    //    QueryExecution result = new QueryExecutionDecoratorTxn<QueryExecution>(tmp, dsg);
    //    return result;
        // UpdateEngine updateEngine = f.create(dsg, null, context);

        Binding initialBinding = BindingRoot.create();

        UpdateProcessorBase tmp = new UpdateProcessorBase(updateRequest, dsg, initialBinding, context, f);
        UpdateProcessor result = UpdateProcessorDecoratorTxn.wrap(tmp, dsg);


        // UpdateProcessor result = updateProcessorFactory.create(updateRequest, dataset, context);
        return result;
    }
}