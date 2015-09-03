package org.aksw.jena_sparql_api.batch;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.shape.ResourceShape;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

public class ListServiceResourceShape
    implements ListService<Concept, Node, Graph>
{
    private QueryExecutionFactory qef;
    private ResourceShape resourceShape;

    @Override
    public List<Entry<Node, Graph>> fetchData(Concept concept, Long limit, Long offset) {

        //ListService

        //Query query = ResourceShape.createQuery(resourceShape, concept);
        //QueryExecution qe = qef.createQueryExecution(query);
        //Model model = qe.execConstruct();

        // TODO Create a map concept

        throw new RuntimeException("not implemented yet");
    }

    @Override
    public CountInfo fetchCount(Concept concept, Long itemLimit) {
        //ListServiceConcept.createQueryCount(concept, itemLimit, n, resultVar)
        throw new RuntimeException("not implemented yet");
    }

}
