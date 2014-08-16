package org.aksw.jena_sparql_api.mapper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class FunctionToMultiMap<K, V>
    implements Function<Map<K, List<V>>, Multimap<K, V>> {
    
    @Override
    public Multimap<K, V> apply(Map<K, List<V>> map) {
        Multimap<K, V> result = HashMultimap.create();
        for(Entry<K, List<V>> entry : map.entrySet()) {
            result.putAll(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

public class AggTransforms {

    public static <K, V> Agg<Multimap<K, V>> multimap(AggMap<K, List<V>> aggMap) {
        Function<Map<K, List<V>>, Multimap<K, V>> transform = new FunctionToMultiMap<K, V>();
        
        AggTransform<Map<K, List<V>>, Multimap<K, V>> result = AggTransform.create(aggMap, transform);
        return result;
    }
}
