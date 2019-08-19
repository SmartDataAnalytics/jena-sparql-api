package org.aksw.jena_sparql_api.rdf.collections;

import org.apache.jena.graph.Node;

public class NodeMapperPassthrough
	implements NodeMapper<Node>
{
	@Override
	public Class<?> getJavaClass() {
		return Node.class;
	}

	@Override
	public boolean canMap(Node node) {
		return true;
	}

	@Override
	public Node toNode(Node obj) {
		return obj;
	}

	@Override
	public Node toJava(Node node) {
		return node;
	}
}
