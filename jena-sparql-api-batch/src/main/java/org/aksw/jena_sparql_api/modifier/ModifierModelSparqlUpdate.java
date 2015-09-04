package org.aksw.jena_sparql_api.modifier;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryModel;
import org.springframework.util.Assert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;


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