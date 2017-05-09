package org.aksw.jena_sparql_api.iso.index;

import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMap;

public class InsertPosition<K> {
    protected GraphIndexNode<K> node;
    protected GraphIsoMap graphIso;

    public InsertPosition(GraphIndexNode<K> node, GraphIsoMap graphIso) {
        super();
        this.node = node;
        this.graphIso = graphIso;
    }

    public GraphIndexNode<K> getNode() {
        return node;
    }

    public GraphIsoMap getGraphIso() {
        return graphIso;
    }

    @Override
    public String toString() {
        return "InsertPosition [node=" + node + ", graphIso=" + graphIso.size() + "]";
    }
}