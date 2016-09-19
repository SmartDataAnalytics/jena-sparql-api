package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;

public interface OpViewMatcher {
	Node add(Op op);

	LookupResult lookupSingle(Op op);
	Collection<LookupResult> lookup(Op op);

}
