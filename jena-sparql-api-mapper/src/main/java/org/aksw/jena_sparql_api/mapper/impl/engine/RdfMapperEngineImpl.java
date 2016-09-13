package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContextImpl;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContextImpl;
import org.aksw.jena_sparql_api.mapper.context.ResolutionRequest;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfPopulatorProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;

public class RdfMapperEngineImpl
    implements RdfMapperEngine
{
    protected Prologue prologue;
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;

    protected RdfTypeFactory typeFactory;
    protected RdfPersistenceContext persistenceContext;


    public RdfMapperEngineImpl(SparqlService sparqlService) {
        this(sparqlService, RdfTypeFactoryImpl.createDefault(), new Prologue(), null); //new RdfPopulationContextImpl());
    }

    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory) {
        this(sparqlService, typeFactory, new Prologue(), null); //new RdfPopulationContextImpl());
    }

    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory, Prologue prologue) {
        this(sparqlService, typeFactory, prologue, null); //new RdfPopulationContextImpl());
    }

//QueryExecutionFactory qef
    public RdfMapperEngineImpl(SparqlService sparqlService, RdfTypeFactory typeFactory, Prologue prologue, RdfPersistenceContext persistenceContext) {
        super();
        this.sparqlService = sparqlService;
        this.typeFactory = typeFactory;
        this.prologue = prologue;
//        this.persistenceContext = persistenceContext != null ? persistenceContext : new RdfPersistenceContextImpl(new FrontierImpl<TypedNode>(), typeFactory);
        this.persistenceContext = new RdfPersistenceContextImpl();
    }


    //@Override
    public RdfPersistenceContext getPersistenceContext() {
        return this.persistenceContext;
    };


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
//Collection<TypedNode> typedNodes
    @Override
    public <T> List<T> list(Class<T> clazz, Concept filterConcept) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();

        List<Node> nodes = ServiceUtils.fetchList(qef, filterConcept);
        
        List<T> result = list(clazz, nodes);
        return result;
    }
    
    public <T> List<T> list(Class<T> clazz, List<Node> nodes) {
        List<T> result = new ArrayList<T>(nodes.size());
        for(Node node : nodes) {
            T entity = find(clazz, node);
            result.add(entity);
        }
        return result;
    }
    
    
    public Graph fetch(RdfType type, Node node) {
        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
        type.exposeShape(builder);
        ResourceShape shape = builder.getResourceShape();
        
        // TODO The lookup service should deal with empty concepts
        Graph result;
        if(!shape.isEmpty()) {
            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null, false);
            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
            LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
            Map<Node, Graph> map = ls.apply(Collections.singleton(node));
            result = map.get(node);
        } else {
            result = null;
        }
        
        if(result == null) {
            result = GraphFactory.createDefaultGraph();
        }
        
        
        return result;
    }
    
    /**
     * Perform a lookup in the persistence context for an entity with id 'node'
     * of type 'clazz'.
     * If no such entity exists, use clazz's corresponding rdfType to fetch
     * triples and instanciate the entity
     * 
     */
    public <T> T find(Class<T> clazz, Node node) {
        Object entity = persistenceContext.entityFor(clazz, node, null);
        
        if(entity == null) {
            RdfType type = typeFactory.forJavaType(clazz);
            
            Graph graph = fetch(type, node);
            entity = type.createJavaObject(node, graph);
            
            Graph refGraph = GraphFactory.createDefaultGraph();
            //Sink<Triple> sink =  new SinkTriplesToGraph(false, refGraph);
            type.populateEntity(persistenceContext, entity, node, graph, refGraph::add);
            
            EntityGraphMap entityGraphMap = persistenceContext.getEntityGraphMap();
            entityGraphMap.putAll(refGraph, entity);
        }
        
        // The call to populateEntity may trigger requests to resolve further entities
        // process them.
        List<ResolutionRequest> requests = persistenceContext.getResolutionRequests();

        // TODO Check for cycles 
        while(!requests.isEmpty()) {
            Iterator<ResolutionRequest> it = requests.iterator();
            ResolutionRequest request = it.next();
            it.remove();
            
            Object resolveEntity = request.getEntity();
            Node resolveNode = request.getNode();
            Class<?> resolveClass = request.getType().getClass();
            Object childEntity = find(resolveClass, resolveNode);
//            String propertyName = request.getPropertyName();
//            
//            EntityOps childEntityOps = request.getEntityOps();
//            PropertyOps childPropertyOps = childEntityOps.getProperty(propertyName);
            PropertyOps childPropertyOps = request.getPropertyOps();
            childPropertyOps.setValue(resolveEntity, childEntity);
        }

        @SuppressWarnings("unchecked")
        T result = (T)entity;
        return result;
    }
        
//        RdfType type = typeFactory.forJavaType(clazz);
//        
//        // TODO Cluster nodes by type for efficiency
//        
//        //for(TypedNode typeNode : typedNodes) {
////           Node rootNode = null;
////            Node rootNode = typeNode.getNode();
//    
//            //Frontier<TypedNode> frontier = new FrontierImpl<TypedNode>();
//            //RdfPersistenceContext persistenceContext = new RdfPersistenceContextFrontier(frontier);
//    
//            EntityGraphMap entityGraphMap = persistenceContext.getEntityGraphMap();
//            //TypedNode first = new TypedNode(rootRdfType, rootNode);
//    
//            //Frontier<TypedNode> frontier = persistenceContext.getFrontier();
//            //frontier.add(first);
//    
//            while(!frontier.isEmpty()) {
//                TypedNode typedNode = frontier.next();
//    
//                RdfType rdfType = typedNode.getRdfType();
//                Node node = typedNode.getNode();
//    
//                ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
//                rdfType.exposeShape(builder);
//    
//    
//                // Fetch the graph
//                QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//    
//                if(!rdfType.isSimpleType()) {
//                    ResourceShape shape = builder.getResourceShape();
//    
//        //            MappedConcept<DatasetGraph> mc = ResourceShape.createMappedConcept2(shape, null);
//        //            LookupService<Node, DatasetGraph> ls = LookupServiceUtils.createLookupService(qef, mc);
//        //            Map<Node, DatasetGraph> map = ls.apply(Collections.singleton(node));
//    
//                    MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null, false);
//                    LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
//                    Map<Node, Graph> map = ls.apply(Collections.singleton(node));
//    
//                    //ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);
//    
//        //            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null);
//        //            ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);
//    
//    
//                    Graph graph = map.get(node);
//    
//                    if(graph != null) {
//                        //DatasetGraph datasetGraph = map.get(node);
//    
//                        Object entity = persistenceContext.entityFor(typedNode);
//                        entityGraphMap.clearGraph(entity);
//    
//                        Graph refs = GraphFactory.createDefaultGraph();
//                        //Sink<Triple> refSink = new SinkTriplesToGraph(false, refs);
//                        //Node subject = rdfType.getRootNode(entity);
//                        Node subject = persistenceContext.getRawRootNode(entity);
//                        rdfType.populateEntity(persistenceContext, entity, subject, graph, refs::add);
//                        //refSink.close();
//    
//                        entityGraphMap.putAll(refs, entity);
//                    }
//                }
//            }
//        //}
//        
//        
//        @SuppressWarnings("unchecked")
//        T result = (T)persistenceContext.getEntity(first);
//
//        return result;

    
//    
//    @Override
//    public <T> T find(Class<T> clazz, Node rootNode) {
//        Concept c = new Concept(new ElementFilter(new E_Equals(new ExprVar(Vars.s), NodeValue.makeNode(rootNode))), Vars.s);
//        List<T> tmp = list(clazz, c);
//        
//        T result;
//
//        int n = tmp.size();
//        switch(n) {
//        case 0: result = null; break;
//        case 1: result = tmp.get(0); break;
//        default: throw new RuntimeException("Only a single entity expected - got " + n + ": " + tmp);
//        }
//        
//        return result;
//    }


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

    
//    public Object getEntity(Node node, RdfType rdfType, Supplier<Object> newInstance) {
//        //persistenceContext.getPrimaryNodeMap()
//        Object result = persistenceContext.entityFor(node, rdfType);
//        if(result == null) {
//            result = newInstance.get();
//        }
//        
//        return result;
//    }
//    
    

    @Override
    public <T> T merge(T tmpEntity) {
        RdfType rootRdfType = typeFactory.forJavaType(tmpEntity.getClass());
        Node node = rootRdfType.getRootNode(tmpEntity);

        T result = merge(tmpEntity, node);
        return result;
    }



    /**
     * Write a given entity as RDF starting with the given node.
     * This will retrieve all triples related triples of the node,
     * perform a diff with the current state and update the backend
     *  
     * 
     */
    @Override
    public <T> T merge(T tmpEntity, Node node) {
        
        //Class<?> entityClazz = type.getClass();
        //Object entity = persistenceContext.entityFor(entityClass, node, () -> type.createJavaObject(node, null));

        /*
         * TODO Copying the properties should make use of EntityOps.copy(...)
         * We may even make use of maps instead of java entities.
         * (Although entities are probably more efficient performance wise)
         */
        
    	Function<Class<?>, EntityOps> entityOpsFactory = ((RdfTypeFactoryImpl)typeFactory).getEntityOpsFactory();
    	
        // TODO We need to perform a recursive merge
    	Object entity = EntityOps.deepCopy(
    			tmpEntity,
    			//clazz -> ((RdfClass)typeFactory.forJavaType(clazz)).getEntityOps(),
    			entityOpsFactory,
    			persistenceContext.getManagedEntities());

        Class<?> entityClass = entity.getClass();
        RdfType type = typeFactory.forJavaType(entityClass);

//        if(entity != tmpEntity) {
//            if(type instanceof RdfClass) {
//            	RdfClass rdfClass = (RdfClass)type;
//            	EntityOps entityOps = rdfClass.getEntityOps();
//            	
//            	for(PropertyOps propertyOps : entityOps.getProperties()) {
//            		Object value = propertyOps.getValue(entity);
//            		
//            		
//            	}
//            	
//            	//EntityOps.copy(entityOps, entityOps, tmpEntity, entity);
//            }
//
//        	
//            //EntityOps.copy()
//            //BeanUtils.copyProperties(tmpEntity, entity);
//        }

        
        // If needed, fetch data corresponding to that entity
        EntityGraphMap entityGraphMap = persistenceContext.getEntityGraphMap();
        Graph graph = entityGraphMap.getGraphForEntity(entity);
        if(graph == null) {
            entity = find(entityClass, node);
            // Request the data for the entity
            graph = fetch(type, node);
            entityGraphMap.putAll(graph, entity);
        }


        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
        String gStr = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
        if(gStr == null) {
            throw new RuntimeException("No target graph specified");
        }
        Node g = NodeFactory.createURI(gStr);

        DatasetGraph newState = DatasetGraphFactory.create();
        Graph outGraph = Quad.defaultGraphIRI.equals(g) ? newState.getDefaultGraph() : newState.getGraph(g);
        //rdfClass.emitTriples(out, entity);
        emitTriples(outGraph, entity);

        
        DatasetGraph oldState = DatasetGraphFactory.create();

        Graph targetGraph = oldState.getGraph(g);
        if(targetGraph == null) {
            targetGraph = GraphFactory.createDefaultGraph();
            oldState.addGraph(g, targetGraph);
        }
        
        
        
//        System.out.println("oldState");
//        DatasetGraphUtils.write(System.out, oldState);
//
//        System.out.println("newState");
//        DatasetGraphUtils.write(System.out, newState);

        Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(newState, oldState);
//        System.out.println("diff: " + diff);
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
        UpdateExecutionUtils.executeUpdate(uef, diff);


//        @SuppressWarnings("unchecked")
//        T result = (T)entity;
//
//        //Node rootNode = persistenceContext.getRootNode(tmpEntity);
//
//
//
//
//        MethodInterceptorRdf interceptor = RdfClass.getMethodInterceptor(entity);
//
//        DatasetGraph oldState = interceptor == null
//                ? DatasetGraphFactory.createMem()
//                : interceptor.getDatasetGraph()
//                ;
//
//                {
//                    EntityGraphMap entityGraphMap = persistenceContext.getEntityGraphMap();
//                    Graph graph = entityGraphMap.getGraphForEntity(entity);
//                    Graph targetGraph = oldState.getGraph(g);
//                    if(targetGraph == null) {
//                        targetGraph = GraphFactory.createDefaultGraph();
//                        oldState.addGraph(g, targetGraph);
//                    }
//
//                    if(graph != null) {
//                        GraphUtil.addInto(targetGraph, graph);
//                    }
//                }
//
//
//        //Class<?> clazz = tmpEntity.getClass();
//        //RdfClass rdfClass = RdfClassFactory.createDefault(prologue).create(clazz);
//        //RdfClass rdfClass = (RdfClass)typeFactory.forJavaType(clazz);
//

        @SuppressWarnings("unchecked")
        T result = (T)entity;
        return result;
    }


    @Override
    public RdfTypeFactory getRdfTypeFactory() {
        return typeFactory;
    }


    @Override
    public void emitTriples(Graph outGraph, Object entity) {
        
    }
    

    @Override
    public void emitTriples(Graph outGraph, Object entity, Node subject) {
        // Check the persistence context for any prior mapping for the given entity
        //persistenceContext.getRootNode(
        
        //Frontier<Object> frontier = FrontierImpl.createIdentityFrontier();
        RdfEmitterContextImpl emitterContext = new RdfEmitterContextImpl(persistenceContext); //frontier);
        //Frontier<Object> frontier = emitterContext.getFrontier();
        
        // Add the initial resolution request
        Node rootNode = emitterContext.requestResolution(entity);

        Map<Node, ResolutionRequest> nodeToResolutionRequest = emitterContext.getNodeToResolutionRequest();
        while(!nodeToResolutionRequest.isEmpty()) {
            // Get and remove first entry
            Iterator<Entry<Node, ResolutionRequest>> it = nodeToResolutionRequest.entrySet().iterator();
            Entry<Node, ResolutionRequest> e = it.next();
            it.remove();
            
            Node node = e.getKey();
            ResolutionRequest request = e.getValue();
            
            Object resolvedEntity = find(request.getType().getClass(), node);
            
            
            
            
        }
        //emitterContext.setEmitted(entity, false);
        //frontier.add(entity);
//
//        while(!frontier.isEmpty()) {
//            Object current = frontier.next();
//
//            Class<?> clazz = current.getClass();
//            RdfType rdfType = typeFactory.forJavaType(clazz);
//
//            // TODO We now need to know which additional
//            // (property) values also need to be emitted
//            //Consumer<Triple> sink = outGraph::add;
//            //emitterContext.ge
//            
//            //Node subject = persistenceContext.getEntity(
//            Node subject = RdfPersistenceContextImpl.getOrCreateRootNode(persistenceContext, typeFactory, current);
//            //Node subject = rdfType.getRootNode(current);
//            
//            //Node subject = emitterContext.getValueN
//            if(subject == null) {
//                throw new RuntimeException("Could not obtain a root node for " + current);
//                //subject = NodeFactory.createURI("http://foobar.foobar");
//            }
//
//            rdfType.emitTriples(emitterContext, current, subject, outGraph::add);
//        }
//        
        // We now need to check the emitterContext for all resources whose 
    }

//    public static Map<Node, Multimap<Node, Node>> getPropertyValue(QueryExecutionFactory qef, RdfPopulator populator) {
//        
//        //populator.exposeShape(shapeBuilder);
//    }


}
