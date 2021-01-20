package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.mapper.Accumulator;

// TODO Implement; requires a lambda to merge values of the same key
public class AccMapBase<K, V>
	implements Accumulator<Entry<K, V>, Map<K, V>>
{

	@Override
	public void accumulate(Entry<K, V> binding) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<K, V> getValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
