package org.aksw.jena_sparql_api.jgrapht.transform;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Graph which supports remapping nodes based on node transforms
 *
 * @author raven
 *
 */
public class GraphNodeRemapBase
    extends WrappedGraph
{
    protected NodeTransform fromGraph;// = new NodeTransformRenameMap(nodeToVar);
    protected NodeTransform toGraph;

//    public static Triple fromGraph(Triple t, Map<Node, Var> nodeToVar) {
//        Triple result = NodeTransformLib.transform(, t);
//        return result;
//    }
//
//    public static Triple toGraph(Triple t, Map<Var, Node> varToNode) {
//        Triple result = NodeTransformLib.transform(new NodeTransformRenameMap(varToNode), t);
//        return result;
//    }

    public GraphNodeRemapBase(Graph graph) {//, NodeTransform fromGraph, NodeTransform toGraph) {
        super(graph);
//        this.fromGraph = fromGraph;
//        this.toGraph = toGraph;
    }

    @Override
    public Graph getWrapped() {
        return base;
    }

    @Override
    public void add(Triple t) {
        Triple u = NodeTransformLib.transform(toGraph, t);
        super.add(u);
    }

    @Override
    public void delete(Triple t) {
        Triple u = NodeTransformLib.transform(toGraph, t);
        super.delete(u);
    }

    @Override
    public boolean contains(Triple t) {
        Triple u = NodeTransformLib.transform(toGraph, t);
        return super.contains(u);
    }


    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        Triple u = NodeTransformLib.transform(toGraph, m);
        return super.find(u).mapWith(v -> NodeTransformLib.transform(fromGraph, v));
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        boolean result = contains(createTriple(s, p, o));
        return result;
    }

    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        ExtendedIterator<Triple> result = find(createTriple(s, p, o));
        return result;
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        delete(createTriple(s, p, o));
    }

    public static Triple createTriple(Node s, Node p, Node o) {
        Triple result = new Triple(
                s == null ? Node.ANY : s,
                p == null ? Node.ANY : p,
                o == null ? Node.ANY : o);
        return result;
    }
}
