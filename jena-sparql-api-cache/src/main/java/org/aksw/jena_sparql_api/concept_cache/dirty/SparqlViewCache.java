package org.aksw.jena_sparql_api.concept_cache.dirty;

import org.aksw.jena_sparql_api.concept_cache.core.CacheResult;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.apache.jena.graph.Node;

/**
 * TODO Extract an interface from this class in order to support
 * different (possibly non-in-memory) backends
 *
 * @author raven
 *
 */
public interface SparqlViewCache<V>
{
    CacheResult lookup(QuadFilterPatternCanonical queryQfpc);
    void put(QuadFilterPatternCanonical qfpc, V value);//QuadFilterPatternCanonical qfpc, Table table);
}
