package org.aksw.jena_sparql_api.batch;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class ListServiceResourceShape
    implements ListService<Concept, Node, Graph>
{
    private QueryExecutionFactory qef;
    private ResourceShape resourceShape;

    public ListServiceResourceShape(QueryExecutionFactory qef,
            ResourceShape resourceShape) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
    }

    @Override
    public Map<Node, Graph> fetchData(Concept concept, Long limit, Long offset) {
        MappedConcept<DatasetGraph> mappedConcept = ResourceShape.createMappedConcept(resourceShape, concept);
        ListService<Concept, Node, DatasetGraph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mappedConcept, true);
        Map<Node, Graph> result = ls.fetchData(concept, limit, offset);
        return result;
    }

    @Override
    public CountInfo fetchCount(Concept concept, Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(qef, concept, itemLimit, rowLimit);
        return result;
    }

    public static ListServiceResourceShape create(QueryExecutionFactory qef, ResourceShape resourceShape) {
        ListServiceResourceShape result = new ListServiceResourceShape(qef, resourceShape);
        return result;
    }
}
