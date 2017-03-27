package org.aksw.jena_sparql_api.views.index;

import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * This class is no longer needed
 * @author raven
 *
 * @param <K>
 */
public class MyEntry<K> {
	protected K id;
	protected Set<Set<String>> featureSets;
	protected OpIndex queryIndex;

	public MyEntry(K id, Set<Set<String>> featureSets, OpIndex queryIndex) {
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