package org.aksw.jena_sparql_api.views;

import java.util.Map;

import org.apache.jena.graph.Node;

public interface Constraint {
	public Constraint copySubstitute(Map<? extends Node, Node> map);
}
