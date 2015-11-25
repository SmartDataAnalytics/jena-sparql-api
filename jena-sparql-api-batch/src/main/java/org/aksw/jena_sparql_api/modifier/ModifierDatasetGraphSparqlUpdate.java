package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDatasetGraph;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

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
