package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

@Deprecated // Use UpdateExecutionFactoryGraphStore instead
public class UpdateExecutionFactoryModel
    extends UpdateExecutionFactoryParsingBase
{
    private Model model;

    public UpdateExecutionFactoryModel(Model model) {
        this.model = model;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {

        Dataset dataset = DatasetFactory.wrap(model);
        UpdateProcessor result = org.apache.jena.update.UpdateExecutionFactory.create(updateRequest, dataset);
        return result;
    }

}
