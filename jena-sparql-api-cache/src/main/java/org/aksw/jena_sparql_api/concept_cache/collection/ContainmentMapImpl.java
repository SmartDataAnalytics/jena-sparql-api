package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

/**
 * Map a set of features to a single value.
 *
 * In
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class ContainmentMapImpl<K, V>
    implements ContainmentMap<K, V>
{

    @Override
    public void put(Set<K> key, V value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<Entry<K, V>> getAllEntriesThatAreSubsetsOf(
            Set<K> prototye) {
        // TODO Auto-generated method stub
        return null;
    }
//    // Map from a tag to all sets containing it
//    protected Multimap<K, Set<K>> tagToTagSets;
//    protected Multimap<Set<K>, V> tagsToValue;
//
//    protected Entry<Set<K>, Set<V>> emptyTagValues; // the key is always an empty set
//
//    @Override
//    public void add(Set<K> key)
//    {
//        if(key.isEmpty()) {
//            emptyTagValues.add(value);
//        } else {
//            for(K k : key) {
//                tagToTagSets.put(k, key);
//            }
//        }
//
//    }
//
//    @Override
//    public Collection<Entry<K, V>> getAllEntriesThatAreSubsetsOf(Set<K> prototype) {
//        Set<Entry<Set<K>, Set<V>>> result;
//
//        if(prototype.isEmpty()) {
//            result = Collections.singleton(emptyTagValues);
//        } else {
//            Map<K, Collection<V>> map = tagToValues.asMap();
//
//            // Map each key to its size and take the smallest one
//            Entry<Set<K>, Set<V>> prototype.stream()
//                .map(k -> new SimpleEntry<>(map.get(k).size()))
//                .min((a, b) -> b.getValue() - a.getValue());
//
//            Entry<Set<K>, Set<V>> result = prototype.stream()
//                    .reduce((Entry<Set<K>, Set<V>>)null, (e, ) -> )
//
//                    // Always append the values of the empty set
//        }
//
//        // Find out the least frequest key
//
//    }

}
