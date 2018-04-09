package org.aksw.jena_sparql_api.utils.model;

import org.apache.jena.graph.Node;

//TODO There is a NodeMapper in the mapper module which only differs from this class by not having a generic type
//However, actually this is quite a difference - because here, the toNode method can be implemented for a specific
// known type, rather than for object
public interface NodeMapper<T> {
	Class<?> getJavaClass();
	public boolean canMap(Node node);

	Node toNode(T obj);
	T toJava(Node node);
}
