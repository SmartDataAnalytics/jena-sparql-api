package org.aksw.jena_sparql_api.shape.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public class ListServiceResourceShape
    implements ListService<Concept, Node, DatasetGraph>
{
    private QueryExecutionFactory qef;
    private ResourceShape resourceShape;
    private boolean isLeftJoin;

    public ListServiceResourceShape(QueryExecutionFactory qef,
            ResourceShape resourceShape,
            boolean isLeftJoin) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
        this.isLeftJoin = isLeftJoin;
    }

    @Override
    public Map<Node, DatasetGraph> fetchData(Concept concept, Long limit, Long offset) {
        MappedConcept<DatasetGraph> mappedConcept = ResourceShape.createMappedConcept2(resourceShape, concept, false);
        ListService<Concept, Node, DatasetGraph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin);
        Map<Node, DatasetGraph> result = ls.fetchData(concept, limit, offset);
        return result;
    }

    @Override
    public CountInfo fetchCount(Concept concept, Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(qef, concept, itemLimit, rowLimit);
        return result;
    }

    public static ListServiceResourceShape create(QueryExecutionFactory qef, ResourceShape resourceShape, boolean isLeftJoin) {
        ListServiceResourceShape result = new ListServiceResourceShape(qef, resourceShape, isLeftJoin);
        return result;
    }
}
