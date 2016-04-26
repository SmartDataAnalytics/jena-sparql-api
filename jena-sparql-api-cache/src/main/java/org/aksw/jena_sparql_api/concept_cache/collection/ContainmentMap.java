package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public interface ContainmentMap<K, V> {
    void put(Set<K> key, V value);

    /**
     *
     * @param prototye
     * @return
     */
    Collection<Entry<K, V>> getAllEntriesThatAreSubsetsOf(Set<K> prototye);
}
