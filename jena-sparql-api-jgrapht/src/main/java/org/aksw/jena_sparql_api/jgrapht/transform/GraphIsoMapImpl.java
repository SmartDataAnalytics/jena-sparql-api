package org.aksw.jena_sparql_api.jgrapht.transform;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

import com.google.common.collect.BiMap;

/**
 * Wrapper around a graph using an (sub-)graph isomorphism mapping
 * @author raven
 *
 */
public class GraphIsoMapImpl
    extends GraphNodeRemapBase
    implements GraphIsoMap
{
    protected BiMap<Node, Node> outToIn;

    public BiMap<Node, Node> getOutToIn() {
        return outToIn;
    }

    public BiMap<Node, Node> getInToOut() {
        return outToIn.inverse();
    }

    public GraphIsoMapImpl(Graph graph, BiMap<Node, Node> outToIn) {
        super(graph);
        this.outToIn = outToIn;

        toGraph = (n) -> outToIn.getOrDefault(n, n);
        fromGraph = (n) -> outToIn.inverse().getOrDefault(n, n);
    }

}
