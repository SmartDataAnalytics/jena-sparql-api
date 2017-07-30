package org.aksw.jena_sparql_api.iso.index;

import java.util.function.Function;

import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMapImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Intersection;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

import com.google.common.collect.BiMap;

public class SetOpsGraphJena
    implements SetOps<Graph, Node>
{
    public static SetOpsGraphJena INSTANCE = new SetOpsGraphJena();

    @Override
    public Graph createNew() {
        return new GraphVarImpl();
    }

    @Override
    public Graph applyIso(Graph set, BiMap<Node, Node> iso) {
        Graph result = new GraphIsoMapImpl(set, iso);
        return result;
    }

    @Override
    public int size(Graph set) {
        int result = set.size();
        return result;
    }

    @Override
    public Graph difference(Graph baseSet, Graph removalSet) {
        Graph result = new Difference(baseSet, removalSet);
        return result;
    }

    @Override
    public Graph intersect(Graph a, Graph b) {
        Graph result = new Intersection(a, b);
        return result;
    }

    @Override
    public Graph transformItems(Graph graph, Function<Node, Node> nodeTransform) {
        NodeTransform tmp = (node) -> nodeTransform.apply(node);
        Graph result = GraphFactory.createDefaultGraph();
        graph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(t -> {
            Triple u = NodeTransformLib.transform(tmp, t);
            result.add(u);
        });
        return result;
    }

}
