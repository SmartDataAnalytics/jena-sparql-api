package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.graph.Node;

import com.google.common.base.Converter;

//TODO There is a NodeMapper in the mapper module which only differs from this class by not having a generic type
//However, actually this is quite a difference - because here, the toNode method can be implemented for a specific
// known type, rather than for object
/**
 * Interface for establishing bidirectional
 * mappings of java objects of a given type T (and its subclasses) to {@link Node}s.
 *
 *
 * @author raven Apr 11, 2018
 *
 * @param <T>
 */
public interface NodeMapper<T>
    extends NodeConverter<Node, T>
{
//	Class<?> getJavaClass();
//	public boolean canMap(Node node);
//
//	Node toNode(T obj);
//	T toJava(Node node);

    default Converter<Node, T> asConverter() {
        return new ConverterFromNodeConverter<Node, T>(this);
    }
}
