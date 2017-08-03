package org.aksw.jena_sparql_api.iso.index;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;


/**
 *
 * @author raven
 *
 * @param <K>
 * @param <G>
 * @param <N>
 */
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
    //Multimap<K, InsertPosition<K, G, N>> lookup(G queryGraph, boolean exactMatch);

    // Add a new entry, thereby allocating a new key
    // TODO This method should maybe not be part of the core interface
    //K add(G graph);

    /**
     * Insert a graph pattern with a specific key, thereby replacing any existing one having this key already
     *
     * @param key
     * @param graph
     * @return
     */
    K put(K key, G graph);

    // Return the set of keys together with the isomorphisms
    //Map<K, Iterable<BiMap<N, N>>> lookupStream(G queryGraph, boolean exactMatch);


    Multimap<K, BiMap<N, N>> lookupX(G queryGraph, boolean exactMatch);

    // Temporary? debug method
    void printTree();


//    Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, G a, G b); // QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());

}
