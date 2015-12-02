package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContextFrontier;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContextFrontier;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.model.RdfPopulatorProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.proxy.MethodInterceptorRdf;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;

public class RdfMapperEngineImpl
    implements RdfMapperEngine
{
    protected Prologue prologue;
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;

    protected RdfTypeFactory typeFactory;
    protected RdfPopulationContext populationContext;

    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory) {
        this(sparqlService, typeFactory, new Prologue(), null); //new RdfPopulationContextImpl());
    }

    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory, Prologue prologue) {
        this(sparqlService, typeFactory, prologue, null); //new RdfPopulationContextImpl());
    }

//QueryExecutionFactory qef
    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory, Prologue prologue, RdfPopulationContext populationContext) {
        super();
        this.sparqlService = sparqlService;
        this.typeFactory = typeFactory;
        this.prologue = prologue;
        this.populationContext = populationContext;
    }


    public Prologue getPrologue() {
        return prologue;
    }

//    public static resolvePopulation(QueryExecutionFactory qef) {
//
//    }

    public <T> LookupService<Node, T> getLookupService(Class<T> clazz) {
        return null;
    }

//    public ListService<Concept, Node, DatasetGraph> prepareListService(RdfClass rdfClass, Concept filterConcept) {
//

    @Override
    public <T> T find(Class<T> clazz, Node rootNode) {
        RdfType rootRdfType = typeFactory.forJavaType(clazz);

        Frontier<TypedNode> frontier = new FrontierImpl<TypedNode>();
        RdfPopulationContext populationContext = new RdfPopulationContextFrontier(frontier);

        TypedNode first = new TypedNode(rootRdfType, rootNode);

        frontier.add(first);

        while(!frontier.isEmpty()) {
            TypedNode typedNode = frontier.next();

            RdfType rdfType = typedNode.getRdfType();
            Node node = typedNode.getNode();

            ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
            rdfType.exposeShape(builder);


            // Fetch the graph
            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();

            if(!rdfType.isSimpleType()) {
                ResourceShape shape = builder.getResourceShape();

    //            MappedConcept<DatasetGraph> mc = ResourceShape.createMappedConcept2(shape, null);
    //            LookupService<Node, DatasetGraph> ls = LookupServiceUtils.createLookupService(qef, mc);
    //            Map<Node, DatasetGraph> map = ls.apply(Collections.singleton(node));


                MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null);
                LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
                Map<Node, Graph> map = ls.apply(Collections.singleton(node));

                //ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);

    //            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null);
    //            ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);


                Graph graph = map.get(node);
                if(graph != null) {
                    //DatasetGraph datasetGraph = map.get(node);

                    Object bean = populationContext.objectFor(typedNode);

                    rdfType.populateBean(populationContext, bean, graph);
                }
            }
        }

        @SuppressWarnings("unchecked")
        T result = (T)populationContext.getEntity(first);

        return result;
    }


//    public MappedConcept<DatasetGraph> getMappedQuery(ResourceShapeBuilder builder, RdfClass rdfClass) {
//
//        Collection<RdfProperty> rdfProperties = rdfClass.getRdfProperties();
//
//        for(RdfProperty rdfProperty : rdfProperties) {
//            processProperty(builder, rdfProperty);
//        }
//
//        ResourceShape shape = builder.getResourceShape();
//        MappedConcept<DatasetGraph> result = ResourceShape.createMappedConcept2(shape, null);
//        return result;
//    }


    public void processProperty(ResourceShapeBuilder builder, RdfPopulatorProperty rdfProperty) {
        //Relation relation = rdfProperty.getRelation();
        //Node predicate = rdfProperty.get
        //builder.outgoing(relation);

        //rdfProperty.getTargetRdfClass()
    }


    @Override
    public <T> T merge(T entity) {
        MethodInterceptorRdf interceptor = RdfClass.getMethodInterceptor(entity);

        DatasetGraph oldState = interceptor == null
                ? DatasetGraphFactory.createMem()
                : interceptor.getDatasetGraph()
                ;

        Class<?> clazz = entity.getClass();
        //RdfClass rdfClass = RdfClassFactory.createDefault(prologue).create(clazz);
        RdfClass rdfClass = (RdfClass)typeFactory.forJavaType(clazz);


        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
        String gStr = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
        if(gStr == null) {
            throw new RuntimeException("No target graph specified");
        }
        Node g = NodeFactory.createURI(gStr);

        DatasetGraph newState = DatasetGraphFactory.createMem();
        Graph outGraph = newState.getGraph(g);
        //rdfClass.emitTriples(out, entity);
        emitTriples(outGraph, entity);

//        System.out.println("oldState");
//        DatasetGraphUtils.write(System.out, oldState);
//
//        System.out.println("newState");
//        DatasetGraphUtils.write(System.out, newState);

        Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(newState, oldState);
//        System.out.println("diff: " + diff);
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
        UpdateExecutionUtils.executeUpdate(uef, diff);

        return entity;
    }


    @Override
    public RdfTypeFactory getRdfTypeFactory() {
        return typeFactory;
    }


    @Override
    public void emitTriples(Graph outGraph, Object entity) {
        Frontier<Object> frontier = FrontierImpl.createIdentityFrontier();
        RdfEmitterContext emitterContext = new RdfEmitterContextFrontier(frontier);

        frontier.add(entity);

        while(!frontier.isEmpty()) {
            Object current = frontier.next();

            Class<?> clazz = current.getClass();
            RdfType rdfType = typeFactory.forJavaType(clazz);

            // TODO We now need to know which additional
            // (property) values also need to be emitted
            rdfType.emitTriples(emitterContext, outGraph, entity);
        }
    }
}
