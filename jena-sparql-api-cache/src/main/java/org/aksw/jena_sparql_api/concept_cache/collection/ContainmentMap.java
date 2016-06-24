package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public interface ContainmentMap<K, V>
    //extends Map<Set<K>, V>
{
    void put(Set<K> tagSet, V value);
    void remove(Object tagSet);

    Set<Set<K>> keySet();
    Collection<V> values();

    Set<Entry<Set<K>, Collection<V>>> entrySet();

    /**
     *
     * @param prototye
     * @return
     */
    Collection<Entry<Set<K>, V>> getAllEntriesThatAreSupersetOf(Set<K> prototype);

    Collection<Entry<Set<K>, V>> getAllEntriesThatAreSubsetOf(Set<K> prototype);
}
