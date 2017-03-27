package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetUtils {


    /**
     * Maps a set of keys to a corresponding set of values via a given map
     * TODO Probably this method can be replaced by something from guava
     *
     * @param set
     * @param map
     * @return
     */
    public static <K, V> Set<V> mapSet(Set<K> set, Map<K, V> map) {
        Set<V> result = new HashSet<V>();
        for(K item : set) {
            V v = map.get(item);
            result.add(v);
        }

        return result;
    }

}
