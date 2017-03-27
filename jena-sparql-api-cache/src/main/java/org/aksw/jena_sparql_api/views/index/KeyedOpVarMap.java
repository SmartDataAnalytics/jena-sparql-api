package org.aksw.jena_sparql_api.views.index;

import java.util.AbstractMap.SimpleEntry;

import org.aksw.jena_sparql_api.view_matcher.OpVarMap;

/**
 * Utility class for an entry comprising a key and OpVarMap attribute.
 *
 *
 * @author raven
 *
 * @param <P>
 */
public class KeyedOpVarMap<K>
	extends SimpleEntry<K, OpVarMap>
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public KeyedOpVarMap(K key, OpVarMap opVarMap) {
		super(key, opVarMap);
	}

	public OpVarMap getOpVarMap() {
		return getValue();
	}
}