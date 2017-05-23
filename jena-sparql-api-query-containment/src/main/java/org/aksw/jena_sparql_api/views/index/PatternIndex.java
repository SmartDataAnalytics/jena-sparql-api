package org.aksw.jena_sparql_api.views.index;

import java.util.Map;


/**
 * Generic pattern index interface
 *
 *
 * @author raven
 *
 * @param <K> A unique ID by which to identify a pattern
 * @param <V> The pattern
 * @param <M> The type of the mapping which relates a matching pattern to a prototype pattern
 */
public interface PatternIndex<K, V, M> {
    //boolean acceptsAdd(Op op);

    /**
     * Retrieve a previously put'd Op by key
     *
     * @param key
     * @return
     */
    V getPattern(K key);

    /**
     * Allocate a new ID for the given Op. May return the ID of a equivalent op.
     * TODO My gut feeling is, that ID allocation should be managed outside of this class.
     *
     * @param op
     * @return
     */
    K allocate(V op);


    /**
     * Put an op with a corresponding key
     *
     * @param key
     * @param op
     */
    void put(K key, V pattern);

    /**
     * The result is expected to be a LinkedHashMap of candidate matches -
     * i.e. the entry set should be ordered, with the 'best' match first
     *
     * @param op
     * @return
     */
    Map<K, M> lookup(V prototype);
    //KeyedOpVarMap<K> lookupSingle(Op op);
    //Collection<KeyedOpVarMap<K>> lookup(Op op);

    /**
     * Remove entry by key
     *
     * @param key
     */
    void removeKey(Object key);
}
