package org.aksw.jena_sparql_api.iso.index;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.collections.set_trie.TagMap;
import org.apache.jena.ext.com.google.common.collect.Sets;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

public class IndexNode<K, G, V, T> {
    // The key associated with the node. null for the root node
    protected K key;

    protected G graph;
    protected Set<T> graphTags;

    // Transitions to target keys
    protected Multimap<K, Edge<K, G, V, T>> targetKeyToEdges;
    protected TagMap<Edge<K, G, V, T>, T> edgeIndex;


    protected Set<K> parents = Sets.newIdentityHashSet();

    void clearLinks(boolean alsoParents) {
        targetKeyToEdges.clear();
        edgeIndex.clear();
        if(alsoParents) {
            parents.clear();
        }
    }

    /**
     * Creates a new node for sub isomorphism indexing.
     *
     * Strategy for indexing the edges to children can be passed via the ctor arg.
     *
     * @param key
     * @param graph
     * @param graphTags
     * @param childIndex
     */
    public IndexNode(K key, G graph, Set<T> graphTags, TagMap<Edge<K, G, V, T>, T> edgeIndex) {
        super();
        this.key = key;
        this.graph = graph;
        this.graphTags = graphTags;
        this.edgeIndex = edgeIndex;
        this.targetKeyToEdges = ArrayListMultimap.create();
    }

    public K getKey() {
        return key;
    }

    public G getGraph() {
        return graph;
    }

    public Set<T> getGraphTags() {
        return graphTags;
    }

    public TagMap<Edge<K, G, V, T>, T> getEdgeIndex() {
        return edgeIndex;
    }


    public void appendChild(IndexNode<K, G, V, T> targetNode, G residualGraph, Set<T> residualGraphTags, BiMap<V, V> transIso) {
        Edge<K, G, V, T> edge = new Edge<>(this.getKey(), targetNode.getKey(), transIso, residualGraph, residualGraphTags);

        targetKeyToEdges.put(targetNode.getKey(), edge);
        edgeIndex.put(edge, residualGraphTags);

        targetNode.getParents().add(key);
    }

//    public Collection<Edge<K, G, V, T>> getEdges() {
//      return keyToChildren.values();
//    }

    public Set<K> getParents() {
        return parents;
    }

    public boolean isLeaf() {
        boolean result = targetKeyToEdges.isEmpty();
        return result;
    }

    public void removeChildById(K targetNodeKey) {
        Collection<Edge<K, G, V, T>> edges = targetKeyToEdges.get(targetNodeKey);
        for(Edge<K, G, V, T> edge : edges) {
            edgeIndex.remove(edge);
        }
        targetKeyToEdges.removeAll(targetNodeKey);
    }

    public Collection<Edge<K, G, V, T>> getEdgesByTargetKey(K targetKey) {
        return targetKeyToEdges.get(targetKey);
    }

    public Multimap<K, Edge<K, G, V, T>> getTargetKeyToEdges() {
        return targetKeyToEdges;
    }
}
