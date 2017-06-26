package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;

/**
 * Aggregated matching of a qfpc.
 * Yields the replacement pattern and the set of matching keys.
 *
 * @author raven
 *
 * @param <V>
 */
public class QfpcAggMatch<K> {
    protected QuadFilterPatternCanonical replacementPattern;
    protected Set<K> keys;

    public QfpcAggMatch(QuadFilterPatternCanonical replacementPattern, Set<K> keys) {
        super();
        this.replacementPattern = replacementPattern;
        this.keys = keys;
    }

    public QuadFilterPatternCanonical getReplacementPattern() {
        return this.replacementPattern;
    }

    public Set<K> getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return "CacheResult [replacementPattern=" + replacementPattern
                + ", keys=" + keys + "]";
    }
}
