package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;

/**
 * TODO Extract an interface from this class in order to support
 * different (possibly non-in-memory) backends
 *
 * @author raven
 *
 */
public interface SparqlViewMatcherQfpc<K>
{
    Map<K, QfpcMatch> lookup(QuadFilterPatternCanonical queryQfpc);
    void put(K key, QuadFilterPatternCanonical qfpc);//QuadFilterPatternCanonical qfpc, Table table);
    void removeKey(Object key);
}
