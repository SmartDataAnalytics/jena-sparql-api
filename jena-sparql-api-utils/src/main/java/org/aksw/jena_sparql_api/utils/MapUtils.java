package org.aksw.jena_sparql_api.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;

import com.google.common.collect.Sets;

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

    public static <K, V> Map<K, Entry<Set<V>, Set<V>>> groupByKey(IBiSetMultimap<K, V> a, IBiSetMultimap<K, V> b) {
        Map<K, Entry<Set<V>, Set<V>>> result = new HashMap<>();
    
        Set<K> keys = Sets.union(a.keySet(), b.keySet());
        keys.forEach(
                k -> {
                    Set<V> av = SetUtils.asSet(a.get(k));
                    Set<V> bv = SetUtils.asSet(b.get(k));
    
                    Entry<Set<V>, Set<V>> e = new SimpleEntry<>(av, bv);
                    result.put(k, e);
                }
            );
    
        return result;
    }

    public static <K, V> Map<K, Entry<Set<V>, Set<V>>> groupByKey(Map<K, ? extends Iterable<V>> a, Map<K, ? extends Iterable<V>> b) {
        Map<K, Entry<Set<V>, Set<V>>> result = new HashMap<>();
    
        Set<K> keys = Sets.union(a.keySet(), b.keySet());
        keys.forEach(
                k -> {
                    Iterable<V> ax = a.get(k);
                    Iterable<V> bx = b.get(k);
                    Set<V> av = ax == null ? Collections.emptySet() : SetUtils.asSet(ax);
                    Set<V> bv = bx == null ? Collections.emptySet() : SetUtils.asSet(bx);
    
                    Entry<Set<V>, Set<V>> e = new SimpleEntry<>(av, bv);
                    result.put(k, e);
                }
            );
    
        return result;
    }




}
