package org.aksw.jena_sparql_api.sparql_path.core;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

public class Context {
	private Node graphNode = Quad.defaultGraphNodeGenerated;

	public Context() {
		super();
	}

	public Node getGraphNode() {
		return graphNode;
	}

	public void setGraphNode(Node graphNode) {
		this.graphNode = graphNode;
	}
	
	
}