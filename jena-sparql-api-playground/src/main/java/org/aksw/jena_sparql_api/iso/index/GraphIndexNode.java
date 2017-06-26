package org.aksw.jena_sparql_api.iso.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

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
public class GraphIndexNode<K>
    //extends LabeledNodeImpl<Long, GraphIndexNode<K>, SubGraphIsomorphismIndex<K>>
{
    protected GraphIndexNode<K> parent;
    protected long id;
    protected BiMap<Node, Node> transIso;
    protected Graph graph;
    protected Set<K> keys = new HashSet<>();
    //protected LinkedList<GraphIndexNode<K>> children = new LinkedList<>();
    protected Map<Long, GraphIndexNode<K>> idToChild = new LinkedHashMap<>();


    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public GraphIndexNode(GraphIndexNode<K> parent, Long id) {
        this.parent = parent;
        this.id = id;
        //super(tree, id);
    }



    public void setParent(GraphIndexNode<K> parent) {
        this.parent = parent;
    }

    public GraphIndexNode<K> getParent() {
        return parent;
    }

    public BiMap<Node, Node> getTransIso() {
        return transIso;
    }

    public long getKey() {
        return id;
    }

    public Graph getValue() {
        return graph;
    }

    public void setTransIso(BiMap<Node, Node> transIso) {
        this.transIso = transIso;
    }

    public void removeChildById(long id) {
        idToChild.remove(id);
    }

    public Collection<GraphIndexNode<K>> getChildren() {
        return idToChild.values();
    }

    public Graph setValue(Graph graph) {//IsoMap graphIso) {
        this.graph = graph;
        //return tree.idToGraph.put(id, graphIso);
        return graph;
    }

    public void appendChild(GraphIndexNode<K> child) {
        if(child.getParent() != null) {
            throw new RuntimeException("Node already has a parent");
        }
        child.setParent(this);

        idToChild.put(child.getKey(), child);
    }

    public Set<K> getKeys() {
        return keys;
        //return tree.idToKeys.get(id);
    }

    @Override
    public String toString() {
        return "GraphIndexNode [id=" + id + ", transIso=" + transIso + ", graph=" + graph + ", keys=" + keys
                + ", children=" + idToChild + "]";
    }



//    public Set<K> setKeys() {
//        return tree.idToKeys.pu
//    }

}