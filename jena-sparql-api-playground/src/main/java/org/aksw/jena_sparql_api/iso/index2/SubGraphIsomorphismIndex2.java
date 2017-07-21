package org.aksw.jena_sparql_api.iso.index2;

import org.aksw.jena_sparql_api.iso.index.InsertPosition;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

/**
 * Implementation of the sub graph isomorphism index which merges all insert DAGs (TODO would it also work with arbitrary graphs)
 * into a global graph.
 *
 *
 *
 * @author raven
 *
 */
//public class SubGraphIsomorphismIndex2<K, G, N>
//    implements SubGraphIsomorphismIndex<K, G, N>
//{
//    protected DirectedGraph<Node, Triple> globalGraph;
//    //protected ReversibleMap<K, G> keyToRootNode;
//
//    public abstract Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, G a, G b);
//
//    @Override
//    public void removeKey(Object key) {
//
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public Multimap<K, InsertPosition<K, G, N>> lookup(G queryGraph, boolean exactMatch) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public K put(K key, DirectedGraph<Node, Triple> graph) {
//        Iterable<BiMap<N, N>> map = match(HashBiMap.create(), graph, globalGraph);
//
//        // For each found isomorphism:
//
//        // Find isomorphism
//        Graphs.addGraph(globalGraph, graph);
//
//
//
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, G a, G b) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//}
