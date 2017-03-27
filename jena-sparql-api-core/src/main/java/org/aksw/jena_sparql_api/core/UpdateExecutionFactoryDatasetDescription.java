package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;

import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecutionFactoryDatasetDescription
	extends UpdateExecutionFactoryDelegate
{
	protected String withIri;
	protected DatasetDescription datasetDescription;

	public UpdateExecutionFactoryDatasetDescription(UpdateExecutionFactory uef, String withIri, DatasetDescription datasetDescription) {
		super(uef);
		this.withIri = withIri;
		this.datasetDescription = datasetDescription;
	}

	@Override
	public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
		UpdateRequest clone = UpdateRequestUtils.clone(updateRequest);

		UpdateRequestUtils.applyWithIri(updateRequest, withIri);
		UpdateRequestUtils.applyDatasetDescription(clone, datasetDescription);

		UpdateProcessor result = super.createUpdateProcessor(updateRequest);
		return result;
	}

}
