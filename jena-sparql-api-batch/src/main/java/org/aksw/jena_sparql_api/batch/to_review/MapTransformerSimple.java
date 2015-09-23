package org.aksw.jena_sparql_api.batch.to_review;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Json simple transformer:
 * $foo: { attrs }
 * -> { attrs } union { type: f(foo) }
 *
 *
 * @author raven
 *
 */
public class MapTransformerSimple
	implements MapTransformer
{
	private Map<String, Object> defaults;

	public MapTransformerSimple() {
		this(new HashMap<String, Object>());
	}

	public MapTransformerSimple(Object ... pairs) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(int i = 0; i < pairs.length; i+=2) {
			String k = (String)pairs[i];
			Object v = pairs[i + 1];
			map.put(k, v);
		}
		this.defaults = map;
	}

	public MapTransformerSimple(Map<String, Object> defaults) {
		this.defaults = defaults;
	}

	@Override
	public Map<String, Object> apply(Map<String, Object> map) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.putAll(map);
		result.putAll(defaults);
		return result;
	}
}