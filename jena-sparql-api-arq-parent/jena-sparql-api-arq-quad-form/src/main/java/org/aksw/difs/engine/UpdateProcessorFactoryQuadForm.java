package org.aksw.difs.engine;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateProcessorBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateProcessorFactoryQuadForm
//	implements UpdateProcessorFactory
{

    // @Override
    public static UpdateProcessor create(UpdateRequest updateRequest, Dataset dataset, Context context) {
        DatasetGraph datasetGraph = dataset.asDatasetGraph();
        Binding inputBinding = null;

        Context cxt = Context.setupContextForDataset(context, datasetGraph);
        UpdateEngineFactory f = UpdateEngineMainQuadForm.getFactory();
                // UpdateEngineRegistry.get().find(datasetGraph, cxt);
        if ( f == null )
            return null;


        // Set the query engine factory for the update processor
        // QC.setFactory(cxt, OpExecutorQuadForm.factory);

        UpdateProcessorBase uProc = new UpdateProcessorBase(updateRequest, datasetGraph, inputBinding, cxt, f);
        return uProc;
    }
}
