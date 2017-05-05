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
    /**
     * Remaps nodes contained *in* this graph to they are exposed differently to the *out*side.
     *
     */
    protected BiMap<Node, Node> inToOut;

    public BiMap<Node, Node> getOutToIn() {
        return inToOut.inverse();
    }

    public BiMap<Node, Node> getInToOut() {
        return inToOut;
    }

    public GraphIsoMapImpl(Graph graph, BiMap<Node, Node> inToOut) {
        super(graph);
        this.inToOut = inToOut;

        toGraph = (n) -> inToOut.inverse().getOrDefault(n, n);
        fromGraph = (n) -> inToOut.getOrDefault(n, n);
    }

}
