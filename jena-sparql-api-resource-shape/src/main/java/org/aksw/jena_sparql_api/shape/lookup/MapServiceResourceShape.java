package org.aksw.jena_sparql_api.shape.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceMapWrapper;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.MapPaginator;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class MapServiceResourceShape
    implements MapService<Concept, Node, Graph>
{
    private SparqlQueryConnection qef;
    private ResourceShape resourceShape;
    private boolean isLeftJoin;

    public MapServiceResourceShape(SparqlQueryConnection qef,
            ResourceShape resourceShape,
            boolean isLeftJoin) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
        this.isLeftJoin = isLeftJoin;
    }

    @Override
    public MapPaginator<Node, Graph> createPaginator(Concept filterConcept) {
        MappedConcept<Graph> mappedConcept = ResourceShape.createMappedConcept(resourceShape, filterConcept, false);
        MapPaginatorMappedConcept<Graph> result = new MapPaginatorMappedConcept<>(qef, null, isLeftJoin, mappedConcept);
        return result;
    }

    public static MapServiceResourceShape create(SparqlQueryConnection qef, ResourceShape resourceShape, boolean isLeftJoin) {
        MapServiceResourceShape result = new MapServiceResourceShape(qef, resourceShape, isLeftJoin);
        return result;
    }



    public static ListService<Concept, Resource> createListService(SparqlQueryConnection qef, ResourceShape resourceShape, boolean isLeftJoin) {
        MapServiceResourceShape base = create(qef, resourceShape, isLeftJoin);

        ListService<Concept, Resource> result = ListServiceMapWrapper.create(base, ResourceUtils::asResource);

//        (node, graph) -> {
//            Model model = ModelFactory.createModelForGraph(graph);
//            Resource r = ModelUtils.convertGraphNodeToRDFNode(node, model).asResource();
//            return r;
//        });

        return result;
    }


    /**
     * Create a lookup service that wraps an instance of this service
     *
     * @param qef
     * @param shape
     * @return
     */
    public static LookupService<Node, Graph> createLookupService(SparqlQueryConnection qef, ResourceShape shape) {
        MapServiceResourceShape base = new MapServiceResourceShape(qef, shape, false);
        LookupService<Node, Graph> result = LookupServiceListService.create(base);

        return result;
    }
}
