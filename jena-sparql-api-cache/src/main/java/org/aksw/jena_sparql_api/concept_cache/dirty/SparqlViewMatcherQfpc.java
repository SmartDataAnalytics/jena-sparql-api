package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Collection;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

/**
 * TODO Extract an interface from this class in order to support
 * different (possibly non-in-memory) backends
 *
 * @author raven
 *
 */
public interface SparqlViewMatcherQfpc<K>
{
    Collection<QfpcMatch<K>> lookup(QuadFilterPatternCanonical queryQfpc);
    void put(K key, QuadFilterPatternCanonical qfpc);//QuadFilterPatternCanonical qfpc, Table table);
    void removeKey(Object key);
}
