package org.aksw.jena_sparql_api.modifier;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDatasetGraph;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/**
 * Retrieve remove to enricht the current model
 *
 * @author raven
 *
 */
public class ModifierDatasetGraphEnrich
    implements Modifier<DatasetGraph>
{
    private LookupService<Node, DatasetGraph> lookupService;

    /**
     * The concept is executed against the input model to select the set of
     * resources to be resolved for enrichment
     */
    private Concept concept;

    public ModifierDatasetGraphEnrich(LookupService<Node, DatasetGraph> lookupService, Concept concept) {
    	this.lookupService = lookupService;
    	this.concept = concept;
    }

    @Override
    public void apply(DatasetGraph input) {
        QueryExecutionFactory qef = new QueryExecutionFactoryDatasetGraph(input, false);
        List<Node> nodes = ServiceUtils.fetchList(qef, concept);

        Map<Node, DatasetGraph> extra = lookupService.apply(nodes);
        for(Entry<Node, DatasetGraph> entry : extra.entrySet()) {
            DatasetGraph m = entry.getValue();
            Iterator<Quad> it = m.find();
            while(it.hasNext()) {
            	Quad quad = it.next();
            	input.add(quad);
            }
        }
    }
}
