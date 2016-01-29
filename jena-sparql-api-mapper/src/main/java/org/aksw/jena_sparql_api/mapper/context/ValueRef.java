package org.aksw.jena_sparql_api.mapper.context;

import org.apache.jena.graph.Node;

public class ValueRef {
	protected Node node;

	public ValueRef(Node node) {
		super();
		this.node = node;
	}

	public Node getNode() {
		return node;
	}
}
