package org.aksw.jena_sparql_api.mapper.context;

import org.apache.jena.graph.Node;

public interface RdfEmitterContext {
	/**
	 * Notify the context about a bean that needs to be emitted
	 * @param bean
	 */
    //void add(Node node, Object entity);

    void add(Object bean, Object parentBean, String propertyName);
	boolean isEmitted(Object bean);
	void setEmitted(Object bean, boolean status);
}
