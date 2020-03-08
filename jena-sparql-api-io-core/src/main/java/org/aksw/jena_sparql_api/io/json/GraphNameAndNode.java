package org.aksw.jena_sparql_api.io.json;

import java.util.Objects;

import org.apache.jena.graph.Node;

public class GraphNameAndNode {
	protected String graphName;
	protected Node node;

	public GraphNameAndNode(String graphName, Node node) {
		super();
		this.graphName = Objects.requireNonNull(graphName);
		this.node = Objects.requireNonNull(node);
	}

	public String getGraphName() {
		return graphName;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "[graphName=" + graphName + ", node=" + node + "]";
	}
}