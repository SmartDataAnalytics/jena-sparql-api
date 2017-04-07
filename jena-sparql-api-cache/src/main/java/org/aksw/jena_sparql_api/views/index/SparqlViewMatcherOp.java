package org.aksw.jena_sparql_api.views.index;

import java.util.Map;

import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.apache.jena.sparql.algebra.Op;

/**
 * Interface for algebra based view matchers.
 * Methods and behavior are similar to that of a map which takes
 * custom keys and Op objects as value.
 * However, lookup methods are provided for retrieving matches (i.e. entries + metadata about the match)
 * by an algebra expression.
 * Implementations of these methods are expected to perform a best-effort in yielding entries, whose
 * associated algebra expression is a sub-tree isomorphism of the given expression.
 *
 *
 *
 * @author raven
 *
 * @param <K>
 */
public interface SparqlViewMatcherOp<K> {
    //boolean acceptsAdd(Op op);

    /**
     * Retrieve a previously put'd Op by key
     *
     * @param key
     * @return
     */
    Op getOp(K key);

    /**
     * Allocate a new ID for the given Op. May return the ID of a equivalent op.
     * TODO My gut feeling is, that ID allocation should be managed outside of this class.
     *
     * @param op
     * @return
     */
    K allocate(Op op);


    /**
     * Put an op with a corresponding key
     *
     * @param key
     * @param op
     */
    void put(K key, Op op);

    /**
     * The result is expected to be a LinkedHashMap of candidate matches -
     * i.e. the entry set should be ordered, with the 'best' match first
     *
     * @param op
     * @return
     */
    Map<K, OpVarMap> lookup(Op op);
    //KeyedOpVarMap<K> lookupSingle(Op op);
    //Collection<KeyedOpVarMap<K>> lookup(Op op);

    /**
     * Remove entry by key
     *
     * @param key
     */
    void removeKey(Object key);
}
