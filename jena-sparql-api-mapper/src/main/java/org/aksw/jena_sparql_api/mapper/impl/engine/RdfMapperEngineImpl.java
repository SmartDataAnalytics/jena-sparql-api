package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
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
import org.aksw.jena_sparql_api.mapper.context.EntityId;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.impl.type.EntityFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PlaceholderInfo;
import org.aksw.jena_sparql_api.mapper.impl.type.PopulationTask;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.mapper.impl.type.ResourceFragment;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.model.ShapeExposable;
import org.aksw.jena_sparql_api.mapper.model.TypeDecider;
import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfMapperEngineImpl
    implements RdfMapperEngine
{
    private static final Logger logger = LoggerFactory.getLogger(RdfMapperEngine.class);

    protected Prologue prologue;
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;

    protected RdfTypeFactory typeFactory;
    //protected RdfPersistenceContext persistenceContext;
    
    protected Map<EntityId, EntityState> originalState = new HashMap<>();
    //protected Map<EntityId, EntityState> currentState = new HashMap<>();


    // TODO Place a configured type decider in here
    protected TypeDecider typeDecider = new TypeDeciderImpl();

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
        //this.persistenceContext = new RdfPersistenceContextImpl();
    }


    public RdfTypeFactory getTypeFactory() {
        return typeFactory;
    }

    //@Override
//    public RdfPersistenceContext getPersistenceContext() {
//        return this.persistenceContext;
//    };


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


    public static RDFNode fetch(Prologue prologue, SparqlService sparqlService, ShapeExposable shapeSupplier, Node node) {
        Map<Node, RDFNode> tmp = fetch(prologue, sparqlService, shapeSupplier, Collections.singletonList(node));
        RDFNode result = tmp.get(node);
        return result;
    }

    public Map<Node, RDFNode> fetch(ShapeExposable shapeSupplier, Collection<Node> nodes) {
        Map<Node, RDFNode> result = fetch(prologue, sparqlService, shapeSupplier, nodes);
        return result;
    }

    public static Map<Node, RDFNode> fetch(Prologue prologue, SparqlService sparqlService, ShapeExposable shapeSupplier, Collection<Node> nodes) {
        //RdfType type = typeFactory.forJavaType(clazz);

        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
        shapeSupplier.exposeShape(builder);
        ResourceShape shape = builder.getResourceShape();

        // TODO The lookup service should deal with empty concepts
        //Graph result;
        Map<Node, RDFNode> result;
        if(!shape.isEmpty()) {
            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null, false);
            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
            LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
            Map<Node, Graph> map = ls.apply(nodes);

            result = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                    e -> {
                    Model m = ModelFactory.createModelForGraph(e.getValue());
                    RDFNode r = ModelUtils.convertGraphNodeToRDFNode(e.getKey(), m);
                    return r;
                }));

        } else {
            result = Collections.emptyMap();
        }


        return result;
    }


    //public Graph fetch(Class<?> clazz, Node node) {
    public Resource fetch(RdfType type, Node node) {
        //RdfType type = typeFactory.forJavaType(clazz);


        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);
        type.exposeShape(builder);
        ResourceShape shape = builder.getResourceShape();

        // TODO The lookup service should deal with empty concepts
        Resource result;
        if(!shape.isEmpty()) {
            MappedConcept<Graph> mc = ResourceShape.createMappedConcept(shape, null, false);
            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
            LookupService<Node, Graph> ls = LookupServiceUtils.createLookupService(qef, mc);
            Map<Node, Graph> map = ls.apply(Collections.singleton(node));            
            Graph g = map.get(node);
            Model m = g == null ? ModelFactory.createDefaultModel() : ModelFactory.createModelForGraph(g);
            RDFNode n = ModelUtils.convertGraphNodeToRDFNode(node, m);
            result = n.asResource();
        } else {
            result = null;
        }

//        if(result == null) {
//        	Model
//            result = 
//        }


        return result;
    }

//    public <T> T find(EntityId entityId) {
//    	Class<?> clazz = entityId.getEntityClass();
//    	Node node = entityId.getNode();
//
//    	Object tmp = find(clazz, node);
//    	T result = (T)tmp;
//    	return result;
//    }

    public static Set<Class<?>> getNonSubsumedClasses(Collection<Class<?>> classes) {
        // Retain all classes which are not a super class of any other
        Set<Class<?>> result = classes.stream()
            .filter(c -> classes.stream().noneMatch(c::isAssignableFrom))
            .collect(Collectors.toSet());

        return result;
    }

    public static Set<Class<?>> getMostSpecificSubclasses(Class<?> given, Collection<Class<?>> classes) {
        // Filter the set by all classes that are a subclass of the given one
        Set<Class<?>> tmp = classes.stream()
            .filter(given::isAssignableFrom)
            .collect(Collectors.toSet());

        Set<Class<?>> result = getNonSubsumedClasses(tmp);
        return result;
    }

    /**
     * Perform a lookup in the persistence context for an entity with id 'node'
     * of type 'clazz'.
     * If no such entity exists, use clazz's corresponding rdfType to fetch
     * triples and instanciate and populate an entity.
     *
     * Will load corresponding triples from the underlying store
     *
     */
    public <T> T find(Class<T> clazz, Node node) {
    	EntityState es = loadEntity(clazz, node);

    	Object o = es.getEntity();
    	@SuppressWarnings("unchecked")
        T result = (T)o;
        return result;
    }
    
    public EntityState loadEntity(Class<?> clazz, Node node) {
        EntityId entityId = new EntityId(clazz, node);

        logger.debug("Entity lookup with " + entityId);

        Objects.requireNonNull(clazz);
    	Objects.requireNonNull(node);
    	

        //System.out.println("Entity lookup with " + node + " " + clazz.getName());

        // Determine if there already exists an (sub-)entity for the given class and node
        //Object entity = persistenceContext.entityFor(clazz, node, null);
        EntityState entityState = originalState.get(entityId);
        Object entity = entityState == null ? null : entityState.getEntity();

        
        // If there is no entity yet, use the type decider to check whether
        // the given node corresponds to a specific sub-class of the given type
        // E.g. If there is request for Object.class, then the type decider may reveal that the
        // given node can be instantiated as a Person.class
        
        // TODO Probably there could be a set of matching classes
        // Options for dealing with this:
        // (a) Instantiate all of them
        // (b) Instantiate any (as this is a subclass of the requested class, any subclass would fit)
        Class<?> actualClazz = clazz;
        if(entity == null) {
            // Determine the set of possible types of node
            // Thereby obtain the shape, fetch data corresponding to the node,
            // obtain java class candidates, and filter by the class requested
            {
                RDFNode rdfNode = fetch(prologue, sparqlService, typeDecider, node);
                if(rdfNode != null) {
                    Collection<Class<?>> classes = typeDecider.getApplicableTypes(rdfNode.asResource());

                    Set<Class<?>> mscs = getMostSpecificSubclasses(clazz, classes);

                    Class<?> tmpClazz;
                    if(mscs.isEmpty()) {
                        throw new RuntimeException("No applicable type found for " + node + " [" + clazz.getName() + "]");
                    } else if(mscs.size() > 1) {
                        throw new RuntimeException("Multiple non-subsumed sub-class candidates of " + clazz + " found: " + mscs);
                    } else {
                        actualClazz = mscs.iterator().next();
                    }
                }
                // TODO select the className which is the most specific sub class of the given class
            }
        }

        RdfType rdfType = typeFactory.forJavaType(actualClazz);

        // If there is no entity yet,
        // instantiate it,
        // fetch the triples
        //
        RDFNode rn;// = entityState.getShapeResource();
        EntityFragment entityFragment;
        if(entity == null) {        
    		rn = fetch(rdfType, node);
    		if(rn == null) {
        		rn = ModelUtils.convertGraphNodeToRDFNode(node, ModelFactory.createDefaultModel());    			
    		}
        	
    		entity = rdfType.createJavaObject(rn);
        } else {
        	rn = entityState.getShapeResource();
        }
        
        if(rn.isResource()) {
        	Resource r = rn.asResource();
        	entityFragment = rdfType.populate(r, entity);
        	
        	//entityFragment = entityState.getEntityFragment();

        	populateEntity(entityFragment);
        } else {
        	entityFragment = new EntityFragment(entity);
        }
        
    	ResourceFragment resourceFragment = null;
        EntityState result = new EntityState(entity, rn, resourceFragment, entityFragment);
    	originalState.put(entityId, result);
    	
    	
    	return result;
    }
    
    void populateEntity(EntityFragment entityFragment) {
        // We now need to construct a function that is capable of resolving
        // the property values of the entity
    	for(PopulationTask task : entityFragment.getTasks()) {
    		List<Object> resolutions = new ArrayList<>();
    		for(PlaceholderInfo placeholder : task.getPlaceholders()) {
            	Class<?> valueClass = placeholder.getTargetRdfType().getEntityClass();
            	RDFNode valueRdfNode = placeholder.getRdfNode();
            	Object value;
            	if(valueRdfNode != null) {
            		if(valueClass == null) {
            			throw new RuntimeException("Should not happen got " + valueRdfNode + " without corresponding java class");
            		}
            		Node valueNode = valueRdfNode.asNode();
            		EntityState valueState = loadEntity(valueClass, valueNode);
            		value = valueState.getEntity();
            	} else {
            		value = null;
            	}
        		resolutions.add(value);
            	//placeholder.getPropertyOps().setValue(entity, value);    			
    		}
    		
    		task.resolve(resolutions);
        }
    }

    public Node resolve(Node s, Node p) {
        ResourceShapeBuilder rsb = new ResourceShapeBuilder();
        rsb.out(p);
        ShapeExposable se = x-> x.getResourceShape();
        Property pp = ResourceFactory.createProperty(p.getURI());
        RDFNode rdfNode = fetch(se, Collections.singleton(s)).get(s);
        rdfNode = rdfNode == null ? null : rdfNode.asResource().getProperty(pp).getObject();
        Node result = rdfNode.asNode();
        //Collection<Node> result =
        return result;
    }

    /*
     *
     * (non-Javadoc)
     * @see org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngine#merge(java.lang.Object)
     */
    @Override
    public <T> T merge(T tmpEntity) {
        RdfType rootRdfType = typeFactory.forJavaType(tmpEntity.getClass());
        Node node = rootRdfType.getRootNode(tmpEntity);

        T result = merge(tmpEntity, node);
        return result;
    }


    public Object find(EntityId entityId) {
        Class<?> entityClass = entityId.getEntityClass();
        Node node = entityId.getNode();
        Object result = find(entityClass, node);
        return result;
    }


    /**
     * From the given entity:
     *   - use its Java Type and the node as an ID (view the node as carrying information about how to populate an instance of the given Java type)
     *   - associate the ID with the graph
     *     - if the triples have not been loaded yet, do so now
     *   - associate the ID with an entity
     *     -
     *
     * Write a given entity as RDF starting with the given node.
     * This will retrieve all triples related triples of the node,
     * perform a diff with the current state.
     *
     *
     */
    @Override
    public <T> T merge(T srcEntity, Node node) {
        Objects.requireNonNull(srcEntity);
        Objects.requireNonNull(node);
        Function<Class<?>, EntityOps> entityOpsFactory = ((RdfTypeFactoryImpl)typeFactory).getEntityOpsFactory();

        Class<?> entityClass = srcEntity.getClass();
        EntityId entityId = new EntityId(entityClass, node);

        EntityState entityState = loadEntity(entityClass, node);
        Object tgtEntity = entityState.getEntity();
       
        
        // TODO tgtEntity might be a subclass of the given entityClass - how to handle this case?
        RdfType type = typeFactory.forJavaType(entityClass);
        RdfClass rdfClass = (RdfClass)type;

        Resource priorState = entityState.getShapeResource().asResource();
        
        ResourceFragment r = new ResourceFragment();        
        rdfClass.exposeFragment(r, priorState, srcEntity);

        Resource exposedS = r.getResource(); 

        // First resolve the placeholders, then populate the entity from the resolved resource
        
        // Perform a breadth-first traversal of the RDF graph and resolve entities
        //TreeUtils.depth(tree)

        Map<RDFNode, PlaceholderInfo> placeholders = r.getPlaceholders();
        
        // Perform a breadth first search of the RDF fragment and resolve
        // placeholder nodes
        Model resolvedModel = ModelFactory.createDefaultModel();
        //Resource superRoot = resolvedModel.createResource();
        Resource unresolvedRoot = r.getResource();
        //Resource resolvedRoot = unresolvedRoot.inModel(resolvedModel);
        Resource resolvedRoot = ModelUtils.convertGraphNodeToRDFNode(node, resolvedModel).asResource();
        //resolvedModel.add(superRoot, RDF.type, r.getResource());
        
        Set<RDFNode> current = Collections.singleton(r.getResource());
        Set<RDFNode> next = new HashSet<>();
        Set<RDFNode> visited = new HashSet<>();

        
        Map<RDFNode, RDFNode> resolutions = new HashMap<>();
        resolutions.put(unresolvedRoot, resolvedRoot);
        
        while(!current.isEmpty()) {
	        for(RDFNode fragNode : current) {
	        	if(visited.contains(fragNode)) {
	        		continue;
	        	}
	        		        	
	        	// Get the set of triples associated with the rdfNode
	        	if(fragNode.isResource()) {
	        		Resource fragS = fragNode.asResource();
	        		for(Statement fragStmt : fragS.listProperties().toList()) {
	        			Property p = fragStmt.getPredicate();
	        			RDFNode fragO = fragStmt.getObject();

	        			Resource s = resolutions.get(fragS).asResource();	        			
	        			
	    	        	PlaceholderInfo info = placeholders.get(fragO);
	    	        	if(info != null) {
		    	        	// NOTE There are two ways to obtain an iri for the entity:
		    	        	// (a) The entity's parent node links via a given property to the to-be reused iri
		    	        	// (b) We invoke the iri generator
		    	        	Object entity = info.getValue();
		    	        	
		    	        	boolean reuseIri = true;
		    	        	RDFNode reusedO = s.getPropertyResourceValue(p);
		    	        	
		    	        	RDFNode o;
		    	        	Function<Map<RDFNode, RDFNode>, RDFNode> iriGenerator = info.getIriGenerator();
		    	        	if(iriGenerator == null) {
		    	        		Node n = info.getTargetRdfType().getRootNode(entity);
		    	        		o = ModelUtils.convertGraphNodeToRDFNode(n, resolvedModel);
		    	        	} else {			    	        	
			    	        	o = reuseIri && reusedO != null
			    	        			? reusedO
			    	        			: info.getIriGenerator().apply(resolutions);	    	        	
		    	        	}
		    	        	
		    	        	
		    	        	// The entity may need recursive resolution	        	
		    	        	//current.add(e);
		    	        	
		    	        	
		    	        	resolutions.put(fragO, o);
			    	        	
		    	        	// TODO get the parent now
		    	        	s.addProperty(p, o);
	    	        	}
	        		}
	        		
	        	}
	        }
	
//	        for(Entry<RDFNode, RDFNode> e : resolutions.entrySet()) {
//	        	//e.getKey().
//	        	ResourceUtils.renameResource(old, uri)
//	        }
        	//ResourceUtils.renameResource(, uri);

	        current = next;
	        next = new HashSet<>();
        }

        
        Resource resolvedS = resolutions.get(exposedS).asResource();
        
        // TODO Make sure to actually resolve any loose ends in the fragment  
        EntityFragment entityFragment = rdfClass.populate(resolvedS, tgtEntity);
        populateEntity(entityFragment);
        
        entityState.setCurrentResource(resolvedS);
        //currentState.put(entityId, value);
        
        commit();
        
        @SuppressWarnings("unchecked")
        T result = (T)tgtEntity;
        return result;
    }

    public void commit() {
        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
        String gStr = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);
        if(gStr == null) {
            throw new RuntimeException("No target graph specified");
        }
        Node g = NodeFactory.createURI(gStr);

        DatasetGraph oldState = DatasetGraphFactory.create();
        DatasetGraph newState = DatasetGraphFactory.create();
        
        for(Entry<EntityId, EntityState> e : originalState.entrySet()) {
        	EntityState state = e.getValue();
        	RDFNode current = state.getCurrentResource();
        	if(current != null) {
            	Graph oldGraph = state.getShapeResource().getModel().getGraph();
        		Graph newGraph = current.getModel().getGraph();
        		        		
        		oldState.addGraph(g, oldGraph);
                newState.addGraph(g, newGraph);
        	}
        	
        }
        
//        Graph targetGraph = oldState.getGraph(g);
//        if(targetGraph == null) {
//            targetGraph = GraphFactory.createDefaultGraph();
//            oldState.addGraph(g, targetGraph);
//        }


        Diff<Set<Quad>> diff = UpdateDiffUtils.computeDelta(newState, oldState);
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
        UpdateExecutionUtils.executeUpdate(uef, diff);

//        Graph outGraph = Quad.defaultGraphIRI.equals(g) ? newState.getDefaultGraph() : newState.getGraph(g);
        //rdfClass.emitTriples(out, entity);
        //emitTriples(outGraph, tgtEntity);

    }
    
//    public void commitOld() {
//        // Recursively merge the entities that appear as values of the srcEntity
//        EntityOps entityOps = entityOpsFactory.apply(entityClass);
//
//
//
//
//        // Copy the attribute values of srcEntity to the allocated tgtEntity
//        for(PropertyOps propertyOps : entityOps.getProperties()) {
//            Object srcValue = propertyOps.getValue(srcEntity);
//            Object tgtValue = propertyOps.getValue(tgtEntity);
//
//
//
//            // Ask the persistence context whether there is already an entity for the target value
//
//            //find(tgtValue);
//
//            EntityId tgtId = null; //persistenceContext.getIdToEntityMap().inverse().get(tgtValue);
//            Object mergedValue;
//            if(tgtId != null) {
//                Node tgtNode = tgtId.getNode();
//                mergedValue = merge(srcValue, tgtNode);
//            } else {
//                mergedValue = srcValue;
//            }
//            if(propertyOps.isWritable()) {
//                propertyOps.setValue(tgtEntity, mergedValue);
//            }
//
//
//        }
//
//    }

        //Class<?> entityClazz = type.getClass();
        //Object entity = persistenceContext.entityFor(entityClass, node, () -> type.createJavaObject(node, null));

        /*
         * TODO Copying the properties should make use of EntityOps.copy(...)
         * We may even make use of maps instead of java entities.
         * (Although entities are probably more efficient performance wise)
         */


        // TODO We need to perform a recursive merge
        //Set<Object> affectedEntities = Sets.newIdentityHashSet();
        // persistenceContext.getManagedEntities()
//    	Object entity = EntityOps.deepCopy(
//    			srcEntity,
//    			//clazz -> ((RdfClass)typeFactory.forJavaType(clazz)).getEntityOps(),
//    			entityOpsFactory,
//    			//affectedEntities,
//    	    	persistenceContext.getManagedEntities()
//
//    			);


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


//        for(RdfPropertyDescriptor pd : rdfClass.getPropertyDescriptors()) {
//            // Check which properties have a corresponding RDF property that leads to a java entity
//            // that has to be merged
//
//            Entry<Node, Object> po = rdfClass.
//
//            String propertyName = pd.getName();
//            PropertyOps propertyOps = entityOps.getProperty(propertyName);
//
//
//            RdfType targetRdfType = pd.getRdfType();
//            Node targetNode;
//            if(!targetRdfType.hasIdentity()) {
//                targetNode = NodeFactory.createURI(node.getURI() + StringUtils.md5Hash(propertyName));
//            } else {
//                targetNode = targetRdfType.getRootNode(srcEntity);
//            }
//
//
//
//            //pm.get
//        }
        //rdfClass.getProperty

//        for(RdfPopulator populator : rdfClass.getPopulators()) {
//          populator.
//        }


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


    @Override
    public RdfTypeFactory getRdfTypeFactory() {
        return typeFactory;
    }

//
//    @Override
//    public void emitTriples(Graph outGraph, Object entity) {
//        EntityId entityId = persistenceContext.getEntityToIdMap().get(entity);
//        if(entityId == null) {
//            throw new RuntimeException("Entity does not have an associated Id: " + entity);
//        }
//
//        // Get all nodes associated with that entity
//
//    }


    /**
     * Emit triples into the outgraph
     *
     * For any referenced entityId (i.e. node and class), the corresponding
     * triples will be fetched first (if not done yet) before emitting that entityId's triples,
     * such that the emit routine can access the prior's entity state.
     *
     *
     */
//    @Override
//    public void emitTriples(Graph outGraph, Object entity, Node subject) {
//        // Check whether the entity was already associated with node
//        // (One java object can only be based on 1 node, however, 1 node may have multiple java objects as views)
//
//
//        //getOrFetchResource()
//
//        // Check the persistence context for any prior mapping for the given entity
//        //persistenceContext.getRootNode(
//
//        //Frontier<Object> frontier = FrontierImpl.createIdentityFrontier();
//        RdfEmitterContextImpl emitterContext = new RdfEmitterContextImpl(persistenceContext); //frontier);
//        //Frontier<Object> frontier = emitterContext.getFrontier();
//
//        // Add the initial resolution request
//        Node rootNode = emitterContext.requestResolution(entity);
//
//        Map<Node, ResolutionRequest> nodeToResolutionRequest = emitterContext.getNodeToResolutionRequest();
//        while(!nodeToResolutionRequest.isEmpty()) {
//            // Get and remove first entry
//            Iterator<Entry<Node, ResolutionRequest>> it = nodeToResolutionRequest.entrySet().iterator();
//            Entry<Node, ResolutionRequest> e = it.next();
//            it.remove();
//
//            Node node = e.getKey();
//            ResolutionRequest request = e.getValue();
//
//            Object resolvedEntity = find(request.getType().getClass(), node);
//
//
//
//
//        }
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


//}





//// Populate the entity from the resource fragment
////RdfClass rdfClass;
//rdfType.exposeFragment(out, priorState, entity);
//
////rdfType.populateEntity(
//
//// The call to populateEntity may trigger requests to resolve further entities
//// process them.
//List<ResolutionRequest> requests = persistenceContext.getResolutionRequests();
//
//// TODO Check for cycles
//while(!requests.isEmpty()) {
//    Iterator<ResolutionRequest> it = requests.iterator();
//    ResolutionRequest request = it.next();
//    it.remove();
//
//    Object resolveEntity = request.getEntity();
//    PropertyOps resolveProperty = request.getPropertyOps();
//    Object resolveValue = resolveProperty.getValue(resolveEntity);
//
//    Node resolveNode = request.getNode();
//    RdfType resolveRdfClass = request.getType();
//    Class<?> resolveClass = resolveRdfClass == null ? (resolveValue == null ? null : resolveValue.getClass()) : resolveRdfClass.getEntityClass();
//
//    if(resolveNode == null) {
//        Class<?> resolveEntityClass = resolveEntity.getClass();
//        RdfType resolveEntityRdfType = typeFactory.forJavaType(resolveEntityClass);
//
//        resolveNode = resolveEntityRdfType.getRootNode(resolveEntity);
//
//        if(resolveClass == null) {
//            resolveClass = resolveEntityClass;
//        }
//
//        // Create a node for the entity
//        System.out.println("oops");
//    }
//
//    if(resolveClass == null || resolveNode == null) {
//        throw new RuntimeException("Should not happen");
//    }
//
//    Object childEntity = find(resolveClass, resolveNode);
//
////    String propertyName = request.getPropertyName();
////
////    EntityOps childEntityOps = request.getEntityOps();
////    PropertyOps childPropertyOps = childEntityOps.getProperty(propertyName);
//    PropertyOps childPropertyOps = request.getPropertyOps();
//    childPropertyOps.setValue(resolveEntity, childEntity);
//}
