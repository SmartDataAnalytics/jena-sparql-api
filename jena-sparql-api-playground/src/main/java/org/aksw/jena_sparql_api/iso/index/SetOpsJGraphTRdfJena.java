package org.aksw.jena_sparql_api.iso.index;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.base.MoreObjects;

public class SetOpsJGraphTRdfJena
    extends SetOpsJGraphTBase<Node, Triple, DirectedGraph<Node, Triple>>
{
    public static final SetOpsJGraphTRdfJena INSTANCE = new SetOpsJGraphTRdfJena();

    @Override
    public DirectedGraph<Node, Triple> createNew() {
        return new SimpleDirectedGraph<>(Triple.class);
    }

    @Override
    protected Triple transformEdge(Triple edge, Function<Node, Node> nodeTransform) {
        //NodeTransform tmp = (node) -> nodeTransform.apply(node);
        NodeTransform tmp = (node) -> MoreObjects.firstNonNull(nodeTransform.apply(node), node);
        Triple result = NodeTransformLib.transform(tmp, edge);

//        System.out.println("Transformed " + edge);
//        System.out.println("  Into " + result);

        return result;
    }

    @Override
    public DirectedGraph<Node, Triple> intersect(DirectedGraph<Node, Triple> baseGraph, DirectedGraph<Node, Triple> removalGraph) {
        DirectedGraph<Node, Triple> result = new DirectedSubgraph<>(baseGraph, removalGraph.vertexSet(), removalGraph.edgeSet());

        //Materialize the intersection
        //DirectedGraph<Node, Triple> tmp = createNew();
        //Graphs.addGraph(tmp, result);
        //result = tmp

        return result;
    }
}
