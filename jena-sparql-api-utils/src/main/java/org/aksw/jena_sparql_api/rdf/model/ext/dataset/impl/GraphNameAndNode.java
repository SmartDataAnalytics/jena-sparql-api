package org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((graphName == null) ? 0 : graphName.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphNameAndNode other = (GraphNameAndNode) obj;
		if (graphName == null) {
			if (other.graphName != null)
				return false;
		} else if (!graphName.equals(other.graphName))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[graphName=" + graphName + ", node=" + node + "]";
	}
}