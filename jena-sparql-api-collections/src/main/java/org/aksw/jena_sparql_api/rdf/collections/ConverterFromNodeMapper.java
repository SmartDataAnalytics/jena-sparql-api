package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.graph.Node;


public class ConverterFromNodeMapper<T>
	extends ConverterFromNodeConverter<Node, T>
{
	public ConverterFromNodeMapper(NodeConverter<Node, T> nodeConverter) {
		super(nodeConverter);
	}
}
