package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.apache.jena.graph.Node;

public interface RdfEmitterContext {
	/**
	 * Notify the context about a bean that needs to be emitted
	 * @param bean
	 */
    //void add(Node node, Object entity);

    
    /**
     * Return a corresponding node for the given entity
     * 
     * Note: The emitter context SPI does not enforce a particular strategy on how to obtain a node.
     * One implementation could directly query the backend and return the final node,
     * whereas other implementations could return placeholders which are resolved at a later stage.
     * 
     * 
     * 
     * @param entity
     * @return
     */
    
    /**
     * Obtain a node that corresponds to the given entity
     * 
     * @param entity
     * @return
     */
    //Node getEntityNode(Object entity);
    
    /**
     * Obtain a node for a specific property value of an entity
     * 
     * @param entity
     * @param propertyName
     * @return
     */
    Node getValueNode(Object entity, String propertyName, Object value);

    
    //void add(Object entity, Object parentBean, String propertyName);
	boolean isEmitted(Object entity);
	void setEmitted(Object entity, boolean status);
	
	Frontier<Object> getFrontier();
}
