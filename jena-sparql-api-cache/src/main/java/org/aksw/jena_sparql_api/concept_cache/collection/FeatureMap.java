package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A feature map associates a set of features with
 * items. Multiple items may have exactly the same features.
 *
 * Hence, this class is called FeatureMap for brevity, but is actually as Feature*Multi*Map.
 * Multimaps can be represented as Collections of entries; i.e. adding multiple entries with the same key is valid.
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public interface FeatureMap<K, V>
    extends Collection<Entry<Set<K>, V>>
    //extends Map<Set<K>, Collection<V>>
    //extends Multimap<Set<K>, V>
{

    default void forEach(BiConsumer<Set<K>, V> consumer) {
        this.forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    void put(Set<K> tagSet, V value);

    boolean remove(Object tagSet);
    boolean removeValue(Object value);

    Set<Set<K>> keySet();
    Collection<V> values();

    Set<Entry<Set<K>, Collection<V>>> entrySet();

    // Get all items having exactly the specified feature set
    Collection<V> get(Set<K> prototype);
    Set<Set<K>> getTagSets(Object v);


    /**
     *
     * @param prototye
     * @return
     */
    Collection<Entry<Set<K>, V>> getIfSupersetOf(Set<K> prototype);

    Collection<Entry<Set<K>, V>> getIfSubsetOf(Set<K> prototype);
}
