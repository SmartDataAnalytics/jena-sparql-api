package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

public interface SparqlViewMatcherPQfpc<K> {

    Map<K, QfpcMatch> lookup(QuadFilterPatternCanonical queryQfpc, VarInfo varInfo);
    void put(K key, QuadFilterPatternCanonical qfpc, VarInfo varInfo);//QuadFilterPatternCanonical qfpc, Table table);
    void removeKey(Object key);

}
