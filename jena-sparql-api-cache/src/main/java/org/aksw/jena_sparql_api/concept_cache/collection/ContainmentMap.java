package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public interface ContainmentMap<K, V>
    //extends Map<Set<K>, V>
{
    void put(Set<K> tagSet, V value);
    void remove(Object tagSet);

    /**
     *
     * @param prototye
     * @return
     */
    Collection<Entry<Set<K>, V>> getAllEntriesThatAreSubsetsOf(Set<K> prototye);
}
