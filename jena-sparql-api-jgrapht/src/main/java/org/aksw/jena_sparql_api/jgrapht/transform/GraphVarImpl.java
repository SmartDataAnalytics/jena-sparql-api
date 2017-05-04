package org.aksw.jena_sparql_api.jgrapht.transform;

import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Graph that allows variables to be inserted - however,
 * they will be mapped to a fresh blank node instead.
 * @author raven
 *
 */
public class GraphVarImpl
    extends WrappedGraph
    implements GraphVar
{
    protected BiMap<Var, Node> varToNode;
    protected Function<Var, Node> nodeGenerator;

    protected transient NodeTransform fromGraph;// = new NodeTransformRenameMap(nodeToVar);
    protected transient NodeTransform toGraph;

//    public static Triple fromGraph(Triple t, Map<Node, Var> nodeToVar) {
//        Triple result = NodeTransformLib.transform(, t);
//        return result;
//    }
//
//    public static Triple toGraph(Triple t, Map<Var, Node> varToNode) {
//        Triple result = NodeTransformLib.transform(new NodeTransformRenameMap(varToNode), t);
//        return result;
//    }

    public GraphVarImpl() {
        this(GraphFactory.createDefaultGraph());
    }

    @Override
    public BiMap<Var, Node> getVarToNode() {
        return varToNode;
    }

    public GraphVarImpl(Graph base) {
        super(base);

        varToNode = HashBiMap.create();
        nodeGenerator = (v) -> NodeFactory.createBlankNode("var_" + v.getName());

        toGraph = (n) -> n.isVariable() ? varToNode.computeIfAbsent((Var)n, (v) -> nodeGenerator.apply(v)) : n;
        fromGraph = (n) -> {
            Var v = varToNode.inverse().get(n);
            Node r = v == null ? n : v;
            return r;
        };
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
        boolean result = contains(new Triple(s, p, o));
        return result;
    }

    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        ExtendedIterator<Triple> result = find(new Triple(s, p, o));
        return result;
    }

}
