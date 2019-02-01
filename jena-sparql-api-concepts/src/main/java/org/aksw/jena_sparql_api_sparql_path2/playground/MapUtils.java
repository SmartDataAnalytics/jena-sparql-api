package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {

    public static <K, V, W> Map<K, Map<V, W>> mergeNestedMap(Map<K, Map<V, W>> a, Map<K, Map<V, W>> b, BinaryOperator<W> mergeFn) {
        Map<K, Map<V, W>> result = mergeMaps(a, b, (x, y) -> mergeMaps(x, y, mergeFn));
        return result;
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> a, Map<K, V> b, BinaryOperator<V> mergeFn) {
        Map<K, V> result = Stream.of(a, b)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        mergeFn));
        return result;
    }

    public static <K, V> void mergeMapsInPlace(Map<K, V> tgt, Map<K, V> src, BinaryOperator<V> mergeFn) {
        src.forEach((k, v) -> {
           V existing = tgt.get(k);
           V newV = existing == null ? v : mergeFn.apply(existing, v);
           tgt.put(k, newV);
        });
    }
}