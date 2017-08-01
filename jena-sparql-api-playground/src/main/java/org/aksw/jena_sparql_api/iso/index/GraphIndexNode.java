package org.aksw.jena_sparql_api.iso.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.set_trie.TagMap;
import org.aksw.commons.collections.set_trie.TagMapSetTrie;

import com.google.common.collect.BiMap;

/**
 *
 *
 * FIXME Materialize if nesting of views gets too deep
 * FIXME Cluster graphs by size (or maybe this is separate indices?) - we don't want to compute isomorphisms from { ?s ?p ?o } to a BGP with 100 TPs
 *
 * Materialization vs recomputation of iso-mappings
 *
 * We check whether a graph being inserted is subsumed by other graphs in regard to isomorphism.
 * Insert graph = graph being inserted
 * Node graph = graph at the current index node
 * As there can be several mappings between in insert graph and the node graph, we can always re-compute the set of iso mappings
 * based on the path to the root.
 *
 *
 *
 *
 * @author raven
 *
 */
public class GraphIndexNode<K, G, N, T>
    //extends LabeledNodeImpl<Long, GraphIndexNode<K>, SubGraphIsomorphismIndex<K>>
{
    protected GraphIndexNode<K, G, N, T> parent;
    protected long id;
    protected BiMap<N, N> transIso;
    protected G graph;
    protected Set<T> graphTags;

    protected Set<K> key;
    //protected Set<K> keys = new HashSet<>();
    //protected LinkedList<GraphIndexNode<K>> children = new LinkedList<>();
    protected Map<Long, GraphIndexNode<K, G, N, T>> idToChild = new LinkedHashMap<>();

    // Tag-based index of the child nodes
    protected TagMap<Long, T> childIndex;

    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public GraphIndexNode(GraphIndexNode<K, G, N, T> parent, Long id, BiMap<N, N> transIso, G graph, Set<T> graphTags, TagMap<Long, T> childIndex) {
        this.parent = parent;
        this.id = id;

        this.transIso = transIso;
        this.graph = graph;
        this.graphTags = graphTags;
        this.childIndex = childIndex;
        //super(tree, id);
    }



    public void setParent(GraphIndexNode<K, G, N, T> parent) {
        this.parent = parent;
    }

    public GraphIndexNode<K, G, N, T> getParent() {
        return parent;
    }

    public BiMap<N, N> getTransIso() {
        return transIso;
    }

    public long getId() {
        return id;
    }

    public G getValue() {
        return graph;
    }

    public Set<T> getGraphTags() {
        return graphTags;
    }

//    public void setTransIso(BiMap<N, N> transIso) {
//        this.transIso = transIso;
//    }

    public void removeChildById(long id) {
        idToChild.remove(id);
        childIndex.remove(id);
    }

    public Collection<GraphIndexNode<K, G, N, T>> getChildren() {
        return idToChild.values();
    }

    public G setValue(G graph) {//IsoMap graphIso) {
        this.graph = graph;
        //return tree.idToGraph.put(id, graphIso);
        return graph;
    }

    public void appendChild(GraphIndexNode<K, G, N, T> child) {
        if(child.getParent() != null) {
            throw new RuntimeException("Node already has a parent");
        }
        child.setParent(this);

        idToChild.put(child.getId(), child);
        childIndex.put(child.getId(), child.getGraphTags());

    }

//    public void setKey(K key) {
//        return key;
//    }
//
//    public K getKey() {
//        return key;
//        //return tree.idToKeys.get(id);
//    }

    @Override
    public String toString() {
        return "GraphIndexNode [id=" + id + ", transIso=" + transIso + ", graph=" + graph + ", graphTags=" + graphTags
                + ", idToChild=" + idToChild + ", childIndex=" + childIndex + "]";
    }


//    public Set<K> setKeys() {
//        return tree.idToKeys.pu
//    }

}