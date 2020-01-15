package org.aksw.jena_sparql_api.conjure.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A bundle of two maps, with the purpose of denoting a primary association between values of K and V
 * while also allowing for relating Vs to Ks.
 * 
 * Use case:
 * The primary file extension for content type 'application/turtle' is 'ttl',
 * However, the file extension 'turtle' is understood as an alternative of that content type.
 * In consequence: writes makes use of the primary file extension,
 * whereas reads can make use of the alternative ones.
 * 
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class MapPair<K, V> {
	protected Map<K, V> primary = new LinkedHashMap<>();
	protected Map<V, K> alternatives = new LinkedHashMap<>();
	
	public MapPair() {
	}

	public Map<K, V> getPrimary() {
		return primary;
	}

	public Map<V, K> getAlternatives() {
		return alternatives;
	}
	
	/**
	 * Convenience method to set a primary mapping and its reverse mapping
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public MapPair<K, V> putPrimary(K key, V value) {
		primary.put(key, value);
		alternatives.put(value, key);
		
		return this;
	}
}