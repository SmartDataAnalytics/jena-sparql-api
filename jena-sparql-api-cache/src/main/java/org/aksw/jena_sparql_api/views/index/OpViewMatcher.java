package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;

import org.apache.jena.sparql.algebra.Op;

public interface OpViewMatcher<K> {
	//boolean acceptsAdd(Op op);

	void put(K key, Op op);

	LookupResult<K> lookupSingle(Op op);
	Collection<LookupResult<K>> lookup(Op op);

	void removeKey(Object key);
	//void remove(V key);
}
