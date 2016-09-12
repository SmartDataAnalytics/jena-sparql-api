package org.aksw.jena_sparql_api.mapper.context;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.aksw.jena_sparql_api.util.frontier.FrontierStatus;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RdfEmitterContextImpl
	implements RdfEmitterContext
{
    protected Frontier<Object> frontier = FrontierImpl.createIdentityFrontier();
    protected RdfPersistenceContext persistenceContext;
    
    //protected Multimap<RdfType, Node> unresolvedNodes = HashMultimap.create();
    
//    protected Map<Node, Triplet<Object, String>> unresolvedValues = new HashMap<>();
    
    
    // Mapping from placeholder nodes to requested resolutions
    protected Map<Node, ResolutionRequest> nodeToResolutionRequest;
    //protected 
    

    protected Map<Object, Node> entityToNode = new IdentityHashMap<>();

    
    // Grouping:
    //
    
    
	//protected EntityContext<? super Object> entityContext;
    //protected Frontier<>
    //protected Map<Object, Node> entityToNode = new IdentityHashMap<>();
    //protected Map<Node,>

	public RdfEmitterContextImpl(RdfPersistenceContext persistenceContext) {
	    this.persistenceContext = persistenceContext;
		//this(EntityContextImpl.createIdentityContext(Object.class));
	    
	}
	
	

//	public RdfEmitterContextImpl(EntityContext<? super Object> entityContext) {
//		this.entityContext = entityContext;
//	}

//	@Override
//	public void add(Object bean, Object parentBean, String propertyName) {
//		//Map<String, Object> map = entityContext.getOrCreate(bean);
//		if(!isEmitted(bean)) {
//			setEmitted(bean, false);
//		}
//
//		// TODO We could keep track of who referenced the bean
//	}

	public Map<Node, ResolutionRequest> getNodeToResolutionRequest() {
        return nodeToResolutionRequest;
    }



//
  public boolean isEmitted(Object entity) {
      return FrontierStatus.DONE.equals(frontier.getStatus(entity));
      //boolean result = entityContext.getAttribute(entity, "isEmitted", false);
      //return result;
  }

  public void setEmitted(Object entity, boolean status) {
      frontier.setStatus(entity, status ? FrontierStatus.DONE : FrontierStatus.OPEN);
      //return FrontierStatus.DONE.equals(frontier.getStatus(entity));
      //entityContext.setAttribute(entity, "isEmitted", status);
  }
//
//	public boolean isEmitted(Object entity) {
//		boolean result = entityContext.getAttribute(entity, "isEmitted", false);
//		return result;
//	}
//
//	public void setEmitted(Object entity, boolean status) {
//		entityContext.setAttribute(entity, "isEmitted", status);
//	}

    public static int i = 1;


    @Override
    public Node requestResolution(Object entity) {
        // Obtain the entity's rdfType
        //persistenceContext.ge
        RdfTypeFactory typeFactory = null;
        Class<?> clazz = entity.getClass();
        RdfType type = typeFactory.forJavaType(clazz);

        Node rootNode = type.getRootNode(entity);
        //Node result = requestResolution(entity, type, () -> rootNode);
        //Node result = persistenceContext.requestResolution(/propertyOps, entity, node);
        
        if(true) {
            throw new RuntimeException("not implemented yet");
        }
        
        return null;
        //return result;
    }
    
//    @Override
//    public Node requestResolution(Object entity, Node node) {
//        // Obtain the entity's rdfType
//        //persistenceContext.ge
//        RdfTypeFactory typeFactory = null;
//        Class<?> clazz = entity.getClass();
//        RdfType type = typeFactory.forJavaType(clazz);
//
//        Node rootNode = type.getRootNode(entity);
//        Node result = requestResolution(entity, type, () -> rootNode);        
//        
//        return result;
//    }
//    

//    public Node requestReuse(Node subject, Node predicate, boolean reverse) {
//        
//    }
//
//    public Node requestReuse(Node subject, Node predicate, boolean reverse) {
//        
//    }

    
    
    
    /**
     * Generate a placeholder node for the value of the property 'property' of entity 'subject'.
     * Resolution of placeholders yields a Map<Node, Node>, mapping placeholders to concrete nodes.
     * 
     * 
     * 
     * Note that subject may again be a placeholder; the engine will attempt to resolve nested placeholder.
     * 
     * @param subject
     * @param property
     */
//    public Node requestPlaceholder(Node subject, Directed<Node> property) {
//        
//    }
    
    /**
     * Request a placeholder node which corresponds to the (inverse) property of the given subject.
     * 
     * Making the strategy for what to do with an existing node configurable is work in progress.
     * 
     * Furthermore, the given java entity triple should be written using the given rdfType with the obtained placeholder node
     * as the starting node.
     * This means, that during writing, further requestResultion requests can be occurr.
     * 
     * 
     * Note, that based on the triples written to the sink, existing iri values may be looked up and possibly reused.
     * Node subject, Directed<Node> property, 
     * 
     * 
     */
//    @Override
//    public Node requestResolution(Object entity, RdfType rdfType, Supplier<Node> iriGenerator) {
//        // Check if we know a mapping for the given value
//        // If not, ask the persistenceContext
//        // If we still have no node, generate one and mark it for future resolution.
//        Node result = entityToNode.get(entity);
//        
//        if(result == null) {
//            result = persistenceContext.getPrimaryNodeMap().get(entity);
//            if(result == null) {
//                result = NodeFactory.createBlankNode();
//                ResolutionRequest request = new ResolutionRequest(entity, rdfType, iriGenerator);
//                
//                nodeToResolutionRequest.put(result, request);
//
//                entityToNode.put(entity, result);
//            }
//        }
//
//        // Unless the value is a primitive object, we need to be able to determine
//        // the value's corresponding node.
//        // However, this can require lookups to the database
//        // In order to be able to perform bulk lookups, we return placeholder nodes
//        // for values for which we don't know the node.
//        
//        // About the placeholders:
//        // Option 1: Always generate placeholders
//        // Option 2: If the persistenceContext holds a node mapping for the value, use this 
//        
//        
//        //System.out.println("Unresolved nodes: " + unresolvedNodes.values());
//        
//        return result;
//    }

    @Override
    public Frontier<Object> getFrontier() {
        return frontier;
    }


//    @Override
//    public void add(Node node, Object entity) {
//        entityToNode.put(entity, node);
//    }
//    
//    public Node get(Object entity, RdfType rdfType) {
//        Node result = entityToNode.get(entity);
//        if(result == null) {
//            result = rdfType.getRootNode(entity);
//            if(result == null) {
//                throw new RuntimeException("Could not obtain a node for: " + entity);
//            }
//        }
//        
//        return result;
//    }

}
