package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {
    public static <K, V> Map<K, V> merge(Map<K, V> a, Map<K, V> b) {
        Map<K, V> result = Stream.of(a, b)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                        //Integer::max
                    )
                )
            ;
        return result;
    }

    public static <K, V> Map<K, V> mergeIfCompatible(Map<K, V> a, Map<K, V> b) {
//        Set<K> diff = Sets.symmetricDifference(a.keySet(), b.keySet());
        boolean isCompatible = org.aksw.commons.collections.MapUtils.isCompatible(a, b);
        Map<K, V> result = isCompatible
                ? merge(a, b)
                : null;
        return result;
    }

}
