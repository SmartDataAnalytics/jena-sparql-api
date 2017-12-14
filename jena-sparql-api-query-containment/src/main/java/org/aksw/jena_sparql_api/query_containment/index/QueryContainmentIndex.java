package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;

import com.google.common.collect.BiMap;

public interface QueryContainmentIndex<K, G, N, A, V> {

	// K is the type of the key for a whole query, whereas Entry<K, Long> is the key
	// referring to a specific leaf in such a query
	SubgraphIsomorphismIndex<Entry<K, Long>, G, N> getIndex();

	void remove(K key);

	void put(K key, A viewOp);

	Stream<Entry<K, TreeMapping<A, A, BiMap<N, N>, V>>> match(A userOp);

}