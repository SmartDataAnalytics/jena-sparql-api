package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.concept_cache.domain.ConjunctiveQuery;

public interface ConjuntiveQueryMatcher<K> {

    Map<K, QfpcMatch> lookup(ConjunctiveQuery cq);
    void put(K key, ConjunctiveQuery cq);
    void removeKey(Object key);

}
