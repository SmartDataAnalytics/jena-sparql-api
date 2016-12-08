package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDatasetGraph;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class ModifierDatasetGraphSparqlUpdate
	implements Modifier<DatasetGraph>
{
	private UpdateRequest updateRequest;

	public ModifierDatasetGraphSparqlUpdate(String updateRequestStr) {
	    this(UpdateFactory.create(updateRequestStr));
	}

	public ModifierDatasetGraphSparqlUpdate(UpdateRequest updateRequest) {
	    this.updateRequest = updateRequest;
	}

	@Override
	public void apply(DatasetGraph dataset) {
	    UpdateExecutionFactoryDatasetGraph uef = new UpdateExecutionFactoryDatasetGraph(dataset);
	    UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
	    updateProcessor.execute();
	}
}
