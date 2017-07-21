package org.aksw.jena_sparql_api.iso.index;

import com.google.common.collect.BiMap;

public interface IsoSetOps<G, N> {
    G applyIso(G base, BiMap<N, N> iso);
}
