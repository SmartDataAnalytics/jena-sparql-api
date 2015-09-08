package org.aksw.jena_sparql_api.lookup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;

public class LookupServiceTransformEntry<KI, KO, V>
	implements LookupService<KO, V>
{
	private LookupService<KO, V> delegate;
	private Function<? extends Entry<KI, V>, KO> keyMapper;
	
	@Override
	public Map<KO, V> apply(Iterable<KI> keys) {
		Map<KO, KI> keyMap = new LinkedHashMap<KO, KI>();
		for(KI ki : keys) {
			KO ko = keyMapper.apply(ki);
			keyMap.put(ko, ki);
		}
		
		Map<KO, V> tmp = delegate.apply(keyMap.keySet());

		Map<KI, V> result = new LinkedHashMap<KI, V>();
		for(Entry<KO, V> entry : tmp.entrySet()) {
			KO ko = entry.getKey();
			V v = entry.getValue();
			
			boolean isMapped = keyMap.containsKey(ko);
			if(!isMapped) {
				throw new RuntimeException("should not happen");
			}
			KI ki = keyMap.get(ko);
			result.put(ki, v);
		}
		
		return result;
	}

}
