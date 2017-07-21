package org.aksw.jena_sparql_api.iso.index;

import com.google.common.collect.BiMap;

public class InsertPosition<K, G, N> {
    protected GraphIndexNode<K, G, N> node;
    protected G residualQueryGraph;
    protected G residualViewGraph;

    protected BiMap<N, N> iso;
    protected BiMap<N, N> latestIsoAB;

    public InsertPosition(
            GraphIndexNode<K, G, N> node,
//            Graph residualViewGraph,
            G residualQueryGraph,
            BiMap<N, N> iso,
            BiMap<N, N> latestIsoAB) {
        super();
        this.node = node;
//        this.residualViewGraph = residualViewGraph;
        this.residualQueryGraph = residualQueryGraph;
        this.iso = iso;
        this.latestIsoAB = latestIsoAB;
    }

    public GraphIndexNode<K, G, N> getNode() {
        return node;
    }

//    public Graph getResidualViewGraph() {
//        return residualViewGraph;
//    }

    public G getResidualQueryGraph() {
        return residualQueryGraph;
    }

    public BiMap<N, N> getIso() {
        return iso;
    }


    public BiMap<N, N> getLatestIsoAB() {
        return latestIsoAB;
    }

    @Override
    public String toString() {
        return "InsertPosition [node=" + node + ", residualQueryGraph=" + residualQueryGraph + ", iso=" + iso + "]";
    }
}