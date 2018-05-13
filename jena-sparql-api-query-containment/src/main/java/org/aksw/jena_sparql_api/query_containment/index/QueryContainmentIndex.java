package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;

/**
 * 
 * 
 * @author raven
 *
 * @param <K> Key type for views
 * @param <G> Graph type (to be removed)
 * @param <V> containment mapping type (usually the type of variables but may also be nodes); TODO Maybe this should comprise the whole Map type?
 * @param <A> algebra expression type
 * @param <R> residual information
 */
public interface QueryContainmentIndex<K, V, A, R, M extends TreeMapping<A, A, BiMap<V, V>, R>> {

	// K is the type of the key for a whole query, whereas Entry<K, Long> is the key
	// referring to a specific leaf in such a query
	// TODO Remove this method - its only there for debugging
	//SubgraphIsomorphismIndex<Entry<K, Long>, G, V> getIndex();

	void remove(K key);

	void put(K key, A viewOp);
	
	/**
	 * Retrieves the possibly transformed algebra expression associated with the key
	 * 
	 * @param key
	 * @return
	 */
	A get(K key);

	/**
	 * The 'Matcher' indirection is used as the index implementation compute an effective op by applying
	 * (semantically equivalent) transformations of the input expression.
	 * 
	 */
//	QueryContainmentMatcher<A, K, M> QueryContainmentMatcher(A userOp);
	
	Stream<Entry<K, M>> match(A userOp);
}