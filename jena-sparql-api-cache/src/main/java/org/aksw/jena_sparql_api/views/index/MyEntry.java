package org.aksw.jena_sparql_api.views.index;

import java.util.Set;

import org.apache.jena.graph.Node;

public class MyEntry<V> {
	public V id;
	public Set<Set<String>> featureSets;
	public OpIndex queryIndex;

	public MyEntry(V id, Set<Set<String>> featureSets, OpIndex queryIndex) {
		super();
		this.id = id;
		this.featureSets = featureSets;
		this.queryIndex = queryIndex;
	}

	@Override
	public String toString() {
		return "Entry [" + id + ", " + featureSets + ", " + queryIndex + "]";
	}
}