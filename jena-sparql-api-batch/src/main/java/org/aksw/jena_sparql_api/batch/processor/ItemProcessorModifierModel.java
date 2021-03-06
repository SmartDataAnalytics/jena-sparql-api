package org.aksw.jena_sparql_api.batch.processor;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.modifier.Modifier;
import org.springframework.batch.item.ItemProcessor;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class ItemProcessorModifierModel
    implements ItemProcessor<Entry<Resource, Model>, Entry<Resource, Model>>
{
    private Modifier<Model> modifier;

    public ItemProcessorModifierModel(Modifier<Model> modifier) {
        this.modifier = modifier;
    }

    @Override
    public Entry<Resource, Model> process(Entry<Resource, Model> item)
            throws Exception {

        Model m = item.getValue();
        modifier.apply(m);
        return item;
    }
}
