package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

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
