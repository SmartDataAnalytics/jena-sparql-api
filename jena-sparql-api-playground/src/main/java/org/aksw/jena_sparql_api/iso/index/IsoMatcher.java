package org.aksw.jena_sparql_api.iso.index;

import com.google.common.collect.BiMap;

public interface IsoMatcher<G, V> {
    Iterable<BiMap<V, V>> match(BiMap<V, V> baseIso, G a, G b);
}
