package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.jena.sparql.algebra.Op;

public interface OpViewMatcher {
	void add(Op op);
	Collection<Entry<Op, OpVarMap>> lookup(Op op);
	
}
