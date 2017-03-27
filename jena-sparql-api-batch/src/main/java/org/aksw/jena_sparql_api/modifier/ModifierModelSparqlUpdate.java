package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryModel;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;


/**
 * Modifies a Model using a SPARQL update request
 *
 * @author raven
 *
 */
public class ModifierModelSparqlUpdate
    implements Modifier<Model>
{
    private UpdateRequest updateRequest;

    public ModifierModelSparqlUpdate(String updateRequestStr) {
        this(UpdateFactory.create(updateRequestStr));
    }

    public ModifierModelSparqlUpdate(UpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
    }

    @Override
    public void apply(Model model) {
        UpdateExecutionFactoryModel uef = new UpdateExecutionFactoryModel(model);
        UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
        updateProcessor.execute();
    }
}