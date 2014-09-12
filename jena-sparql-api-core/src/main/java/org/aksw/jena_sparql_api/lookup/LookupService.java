package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

public interface LookupService<K, V> {
    Map<K, V> lookup(Iterable<K> keys);
}
