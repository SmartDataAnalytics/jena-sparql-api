package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Dataset;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class ModifierDatasetSparqlUpdate
	implements Modifier<Dataset>
{
	private UpdateRequest updateRequest;

	public ModifierDatasetSparqlUpdate(String updateRequestStr) {
	    this(UpdateFactory.create(updateRequestStr));
	}

	public ModifierDatasetSparqlUpdate(UpdateRequest updateRequest) {
	    this.updateRequest = updateRequest;
	}

	@Override
	public void apply(Dataset dataset) {
	    UpdateExecutionFactoryDataset uef = new UpdateExecutionFactoryDataset(dataset);
	    UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
	    updateProcessor.execute();
	}
}
