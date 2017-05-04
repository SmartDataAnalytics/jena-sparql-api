package org.aksw.jena_sparql_api.shape.lookup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.Paginator;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;

import com.google.common.collect.Range;



//class PaginatorSparqlBase<K, V> {
//    protected QueryExecutionFactory qef;
//    protected ResourceShape resourceShape;
//    protected boolean isLeftJoin;
//
//}
//

class PaginatorSparqlShapeModel<K, V>
    implements RangedSupplier<Comparable<Long>, Map<K, V>>
{
    protected QueryExecutionFactory qef;
    protected ResourceShape resourceShape;
    protected boolean isLeftJoin;
    protected Concept concept;

    //@Override
    public List<Resource> apply(Range<Long> rang) {
        Map<Node, Graph> map = ls.fetchData(concept, limit, offset);

        List<Resource> result = map.entrySet().stream().map(e -> {
            Model m = ModelFactory.createModelForGraph(e.getValue());
            Resource r = ModelUtils.convertGraphNodeToRDFNode(e.getKey(), m).asResource();
            return r;
        }).collect(Collectors.toList());

        return result;
    }

    //@Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(qef, concept, itemLimit, rowLimit);
        return result;
    }

}

public class ListServiceResourceShapeModel
    //implements ListService<Concept, Resource>
{
    private QueryExecutionFactory qef;
    private ResourceShape resourceShape;
    private boolean isLeftJoin;

    public ListServiceResourceShapeModel(QueryExecutionFactory qef,
            ResourceShape resourceShape,
            boolean isLeftJoin) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
        this.isLeftJoin = isLeftJoin;
    }

    public Paginator<Node, Graph> createPaginator(Concept concept) {
        MappedConcept<Graph> mappedConcept = ResourceShape.createMappedConcept(resourceShape, concept, false);
        ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin);


    }

    //@Override
//    public List<Resource> fetchData(Concept concept, Long limit, Long offset) {
//        MappedConcept<Graph> mappedConcept = ResourceShape.createMappedConcept(resourceShape, concept, false);
//        ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mappedConcept, isLeftJoin);
//        Map<Node, Graph> map = ls.fetchData(concept, limit, offset);
//
//        List<Resource> result = map.entrySet().stream().map(e -> {
//            Model m = ModelFactory.createModelForGraph(e.getValue());
//            Resource r = ModelUtils.convertGraphNodeToRDFNode(e.getKey(), m).asResource();
//            return r;
//        }).collect(Collectors.toList());
//
//        return result;
//    }

    //@Override
    public CountInfo fetchCount(Concept concept, Long itemLimit, Long rowLimit) {
        CountInfo result = ServiceUtils.fetchCountConcept(qef, concept, itemLimit, rowLimit);
        return result;
    }

    public static ListServiceResourceShape create(QueryExecutionFactory qef, ResourceShape resourceShape, boolean isLeftJoin) {
        ListServiceResourceShape result = new ListServiceResourceShape(qef, resourceShape, isLeftJoin);
        return result;
    }

}
