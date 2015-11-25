package org.aksw.jena_sparql_api.batch.processor;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.modifier.Modifier;
import org.springframework.batch.item.ItemProcessor;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class ItemProcessorModifierDatasetGraph
    implements ItemProcessor<Entry<Node, DatasetGraph>, Entry<Node, DatasetGraph>>
{
    private Modifier<? super DatasetGraph> modifier;

    public ItemProcessorModifierDatasetGraph(Modifier<? super DatasetGraph> modifier) {
        this.modifier = modifier;
    }

    @Override
    public Entry<Node, DatasetGraph> process(Entry<Node, DatasetGraph> item)
            throws Exception {

        DatasetGraph m = item.getValue();
        modifier.apply(m);
        return item;
    }
}
