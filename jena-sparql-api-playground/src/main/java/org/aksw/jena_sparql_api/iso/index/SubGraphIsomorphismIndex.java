package org.aksw.jena_sparql_api.iso.index;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;


public interface SubGraphIsomorphismIndex<K, G, N> {

    void removeKey(Object key);

    /**
     * For each key, returns sets of objects each comprising:
     * - the index node, holding the residual index graph
     * - the isomorphism from the residual index graph to the residual query graph
     * - the residual query graph
     *
     *
     *
     * @param queryGraph
     * @return
     */
    Multimap<K, InsertPosition<K, G, N>> lookup(G queryGraph, boolean exactMatch);

    /**
     * Insert a graph pattern with a specific key, thereby replacing any existing one having this key already
     *
     * @param key
     * @param graph
     * @return
     */
    K put(K key, G graph);

    Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, G a, G b); // QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());

}
