package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;

public class UpdateProcessorRunnable
	implements UpdateProcessor
{
	protected Context context;
	protected DatasetGraph datasetGraph;
	protected Runnable delegate;
	
	public UpdateProcessorRunnable(Context context, DatasetGraph datasetGraph, Runnable delegate) {
		super();
		this.context = context;
		this.datasetGraph = datasetGraph;
		this.delegate = delegate;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public DatasetGraph getDatasetGraph() {
		return datasetGraph;
	}

	@Override
	public void execute() {
		delegate.run();
	}
}