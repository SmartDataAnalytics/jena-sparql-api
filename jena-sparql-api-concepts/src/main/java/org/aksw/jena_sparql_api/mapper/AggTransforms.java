package org.aksw.jena_sparql_api.mapper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

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
    public static final Gson defaultGson = new Gson();

    public static <K, V> Agg<Multimap<K, V>> multimap(AggMap<K, List<V>> aggMap) {
        Function<Map<K, List<V>>, Multimap<K, V>> transform = new FunctionToMultiMap<K, V>();

        AggTransform<Map<K, List<V>>, Multimap<K, V>> result = AggTransform.create(aggMap, transform);
        return result;
    }

    public static <V, T> Agg<T> clazz(Class<T> cl, Agg<V> agg) {
        Agg<T> result = clazz(cl, agg, defaultGson);
        return result;
    }

    public static <V, T> Agg<T> clazz(Class<T> cl, Agg<V> agg, Gson gson) {
        Function<V, T> fn = new FunctionObjectToClass<V, T>(gson, cl);

        AggTransform<V, T> result = AggTransform.create(agg, fn);
        return result;
    }
}
