package org.aksw.jena_sparql_api.iso.index;

import java.util.Set;

import com.google.common.collect.BiMap;

class Edge<K, G, V, T>
{
    protected K from;
    protected K to;
    protected BiMap<V, V> transIso;

    // The remaining graph tags at this node
    protected Set<T> residualGraphTags;
    protected G residualGraph;

    public Edge(K from, K to, BiMap<V, V> transIso, G residualGraph, Set<T> residualGraphTags) {
        super();
        this.from = from;
        this.to = to;
        this.transIso = transIso;
        this.residualGraph = residualGraph;
        this.residualGraphTags = residualGraphTags;
    }

    public K getFrom() {
        return from;
    }

    public K getTo() {
        return to;
    }

    public BiMap<V, V> getTransIso() {
        return transIso;
    }

    public G getResidualGraph() {
        return residualGraph;
    }

    public Set<T> getResidualGraphTags() {
        return residualGraphTags;
    }
}