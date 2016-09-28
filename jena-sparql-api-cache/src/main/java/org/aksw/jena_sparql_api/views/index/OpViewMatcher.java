package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;

public interface OpViewMatcher<V> {
	//boolean acceptsAdd(Op op);

	void put(Op op, V value);

	LookupResult lookupSingle(Op op);
	Collection<LookupResult> lookup(Op op);

}
