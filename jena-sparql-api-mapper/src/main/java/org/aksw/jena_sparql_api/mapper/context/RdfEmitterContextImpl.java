package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.aksw.jena_sparql_api.util.frontier.FrontierStatus;
import org.apache.jena.graph.Node;

public class RdfEmitterContextImpl
	implements RdfEmitterContext
{
    protected Frontier<Object> frontier = FrontierImpl.createIdentityFrontier();
    
	//protected EntityContext<? super Object> entityContext;
    //protected Frontier<>
    //protected Map<Object, Node> entityToNode = new IdentityHashMap<>();
    //protected Map<Node,>

	public RdfEmitterContextImpl() {
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

    @Override
    public Node getValueNode(Object entity, String propertyName, Object value) {
        // Unless the value is a primitive object, we need to be able to determine
        // the value's corresponding node.
        // However, this can require lookups to the database
        // In order to be able to perform bulk lookups, we return placeholder nodes
        // for values for which we don't know the node.
        
        // About the placeholders:
        // Option 1: Always generate placeholders
        // Option 2: If the persistenceContext holds a node mapping for the value, use this 
        
        
        return null;
    }

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
