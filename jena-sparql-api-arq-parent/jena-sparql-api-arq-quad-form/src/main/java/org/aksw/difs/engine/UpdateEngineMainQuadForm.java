package org.aksw.difs.engine;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineMain;
import org.apache.jena.sparql.modify.UpdateEngineWorker;
import org.apache.jena.sparql.modify.request.UpdateVisitor;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineMainQuadForm
    extends UpdateEngineMain
{
    public UpdateEngineMainQuadForm(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        super(datasetGraph, inputBinding, context);
    }

    protected UpdateVisitor prepareWorker() {
        return new UpdateEngineWorkerQuadForm(datasetGraph, inputBinding, context) ;
    }


    public static UpdateEngineFactory getFactory() {
        return new UpdateEngineFactory()
        {
            @Override
            public boolean accept(DatasetGraph dataset, Context context) {
                return (dataset instanceof DatasetGraph);
            }

            @Override
            public UpdateEngine create(DatasetGraph dataset, Binding inputBinding, Context context) {
                return new UpdateEngineMainQuadForm(dataset, inputBinding, context);
            }
        };
    }

}
