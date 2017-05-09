package org.aksw.jena_sparql_api.iso.index;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.common.collect.BiMap;

public class InsertPosition<K> {
    protected GraphIndexNode<K> node;
    protected Graph residualQueryGraph;
    protected Graph residualViewGraph;

    protected BiMap<Node, Node> iso;

    public InsertPosition(
            GraphIndexNode<K> node,
//            Graph residualViewGraph,
            Graph residualQueryGraph,
            BiMap<Node, Node> iso) {
        super();
        this.node = node;
//        this.residualViewGraph = residualViewGraph;
        this.residualQueryGraph = residualQueryGraph;
        this.iso = iso;
    }

    public GraphIndexNode<K> getNode() {
        return node;
    }

//    public Graph getResidualViewGraph() {
//        return residualViewGraph;
//    }

    public Graph getResidualQueryGraph() {
        return residualQueryGraph;
    }

    public BiMap<Node, Node> getIso() {
        return iso;
    }

    @Override
    public String toString() {
        return "InsertPosition [node=" + node + ", residualQueryGraph=" + residualQueryGraph + ", iso=" + iso + "]";
    }
}