package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.util.ValueHolder;
import org.aksw.jena_sparql_api.mapper.util.ValueHolderImpl;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

public class RdfTypeMap
    //extends RdfPopulatorPropertyBase
    extends RdfTypeComplexBase
{
    public static final Property entry = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/entry");
    public static final Property key = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/key");
    public static final Property value = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/value");

    public static final Property keyClass = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/keyClass");
    public static final Property valueClass = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/valueClass");

//    protected RdfType keyRdfType;
//    protected RdfType valueRdfType;
    protected Class<?> keyClazz;
    protected Class<?> valueClazz;
    
    //protected MapOps mapOps;
    protected Function<Object, Map> createMapView;

    // , PropertyOps propertyOps, Node predicate, RdfType targetRdfType
    
    public RdfTypeMap(
        	Function<Object, Map> createMapView)
    {
    	this(createMapView, Object.class, Object.class);
    }
    
    public RdfTypeMap(
    	Function<Object, Map> createMapView,
//    	RdfType keyRdfType,
//    	RdfType valueRdfType
    	Class<?> keyClazz,
    	Class<?> valueClazz
    	) {
        ///super(typeFactory);
        this.createMapView = createMapView;
//        this.keyRdfType = keyRdfType;
//        this.valueRdfType = valueRdfType;
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
    }


    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        ResourceShapeBuilder tmp = shapeBuilder.out(entry.asNode());

        tmp.out(key.asNode());
        tmp.out(value.asNode());
    }

//    @Override
//    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject,
//            Graph shapeGraph, Consumer<Triple> sink) {

    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
        @SuppressWarnings("unchecked")
        Map<? super Object, ? super Object> map = createMapView.apply(entity);


        int i = 1;
        for(Entry<?, ?> e : map.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();

            Resource subject = out.getResource();
            Model m = subject.getModel();

            Resource r = m.createResource(subject.getURI() + "-" + i); 
            
            Resource kNode = m.createResource();
            Resource vNode = m.createResource();
            
            subject.addProperty(entry, r);
            
            r
            	.addProperty(key, kNode)
            	.addProperty(value, vNode);

//            ValueHolder vh = new ValueHolderImpl(
//            	() -> map.get(k),
//            	x -> map.put(k, x)
//            );
            
            out.getPlaceholders().put(kNode, new PlaceholderInfo(keyClazz, null, entity, null, k, null, null));
            out.getPlaceholders().put(vNode, new PlaceholderInfo(valueClazz, null, entity, null, v, null, null));

            ++i;
        }

    }
    
    /**
     * The fragment will contain information about which nodes need to be resolved.
     * Once everything is resolved, there needs to be a function that carries
     * out the actualy population - so its more like
     * 
     * Populator populator = exposePopulator(shape, entity) // Maybe the entity is not needed at this stage
     * 
     * populator.refs.forEach((key, class, node) -> context.put(key, rdfMapperEngine.resolve(class, node)))
     * populator.resolve(context)
     * 
     * 
     */
	@Override
	public EntityFragment populate(Resource shape, Object entity) {
	
	    // <Object, Object>
	    Map<Object, Object> map = createMapView.apply(entity);
	

	    EntityFragment result = new EntityFragment(entity);
	    for(Statement stmt : shape.listProperties(entry).toList()) {
	        Resource e = stmt.getObject().asResource();
	
	        Node kNode = e.getProperty(key).getObject().asNode();
	        Node vNode = e.getProperty(value).getObject().asNode();
	
	        // TODO: We need to dynamically figure out which entity the node could be
	        EntityPlaceholderInfo placeholder = new EntityPlaceholderInfo(valueClass, entity, parentRes, propertyOps, vNode, null);
	        //RdfType rdfType = null;
	        //Object k = persistenceContext.entityFor(Object.class, kNode, null);//new TypedNode(rdfType, kNode));          Object v = persistenceContext.entityFor(Object.class, vNode, null);//new TypedNode(rdfType, vNode));
	
	        Object k = null;
	        ValueHolder vh = new ValueHolderImpl(
	        		() -> map.get(k),
	        		v -> map.put(k, v)	        		
	        );
	        
//	        map.put(k, v);
	    }
	    
	    return result;
	}


    @Override
    public Class<?> getEntityClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getRootNode(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Object createJavaObject(RDFNode node) {
        @SuppressWarnings("rawtypes")
        Map result = new HashMap();
        return result;
    }


    @Override
    public boolean hasIdentity() {
        // TODO Auto-generated method stub
        return false;
    }

//    @Override
//    public Object createJavaObject(Node node) {
//        //entityOps.
//        // TODO Auto-generated method stub
//        return null;
//    }

}

//
//@Override
//public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Node subject, Graph graph, Consumer<Triple> outSink) {
//  Model model = ModelFactory.createModelForGraph(graph);
//  RDFNode root = ModelUtils.convertGraphNodeToRDFNode(subject, model);
//
//  // <Object, Object>
//  Map map = createMapView.apply(entity);
//
//
//  for(Statement stmt : root.asResource().listProperties(entry).toList()) {
//      Resource e = stmt.getObject().asResource();
//
//      Node kNode = e.getProperty(key).getObject().asNode();
//      Node vNode = e.getProperty(value).getObject().asNode();
//
//
//      // TODO: We need to dynamically figure out which entity the node could be
//      RdfType rdfType = null;
//      Object k = persistenceContext.entityFor(Object.class, kNode, null);//new TypedNode(rdfType, kNode));
//      Object v = persistenceContext.entityFor(Object.class, vNode, null);//new TypedNode(rdfType, vNode));
//
//      map.put(k, v);
//  }
//}

