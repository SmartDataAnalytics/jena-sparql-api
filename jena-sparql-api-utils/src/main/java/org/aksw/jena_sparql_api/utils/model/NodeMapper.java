package org.aksw.jena_sparql_api.utils.model;

import java.util.Objects;

import org.apache.jena.graph.Node;

//TODO There is a NodeMapper in the mapper module which only differs from this class by not having a generic type
//However, actually this is quite a difference - because here, the toNode method can be implemented for a specific
// known type, rather than for object
/**
 * Interface for establishing bidirectional
 * mappings of java objects of a given type T to {@link Node}s.
 * 
 * 
 * @author raven Apr 11, 2018
 *
 * @param <T>
 */
public interface NodeMapper<T> 
{
	Class<?> getJavaClass();
	public boolean canMap(Node node);
	
	Node toNode(T obj);
	T toJava(Node node);
	
	default Node toNodeFromObject(Object obj) {
		Objects.requireNonNull(obj);
		Class<?> clazz = getJavaClass();
		@SuppressWarnings("unchecked")
		Node result = clazz.isAssignableFrom(obj.getClass())
				? toNode((T)obj)
				: null;

		return result;
	}
}
