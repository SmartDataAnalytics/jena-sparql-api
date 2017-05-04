package org.aksw.jena_sparql_api.jgrapht.transform;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;


/**
 * We can directly convert BGPs and expressions to an RDF graph.
 * The sub graph isomorphism check can be done using the jena wrapper for jgrapht.
 *
 *
 * @author raven
 *
 */
public class QueryToJenaGraph {

    public static Node unionMember = NodeFactory.createURI("http://ex.org/unionMember");
    public static Node quadBlockMember = NodeFactory.createURI("http://ex.org/quadBlockMember");
    public static Node filtered = NodeFactory.createURI("http://ex.org/filtered");

    public static Node TP = NodeFactory.createURI("http://ex.org/TP");
    //public static Node TP = NodeFactory.createURI("http://ex.org/TP");

    public static Node g = NodeFactory.createURI("http://ex.org/g");
    public static Node s = NodeFactory.createURI("http://ex.org/s");
    public static Node p = NodeFactory.createURI("http://ex.org/p");
    public static Node o = NodeFactory.createURI("http://ex.org/o");

    public static Node[] gspo = {g, s, p, o};

    public static Node dm = NodeFactory.createURI("http://ex.org/dm"); // disjunction member

    public static Node ev = NodeFactory.createURI("http://ex.org/ea"); // equality var argument
    public static Node ec = NodeFactory.createURI("http://ex.org/ea"); // equality const argument


    //public static unionToGraph(DirectedGraph<Node, LabeledEdge<Node, Node>>)

    public static void addEdge(Graph graph, Node source, Node edgeLabel, Node target, Supplier<Node> nodeSupplier, Map<Var, Node> varToNode) {
//        Node o = target.isVariable()
//                ? varToNode.computeIfAbsent((Var)target, (var) -> nodeSupplier.get())
//                : target;

        graph.add(new Triple(source, edgeLabel, target));
    }

    public static Node addQuad(Graph graph, Quad quad, Supplier<Node> nodeSupplier, Map<Var, Node> varToNode) {
        // Allocate a fresh node for the quad
        Node quadNode = nodeSupplier.get();//NodeFactory.createBlankNode();

        addEdge(graph, quadNode, g, quad.getGraph(), nodeSupplier, varToNode);
        addEdge(graph, quadNode, s, quad.getSubject(), nodeSupplier, varToNode);
        addEdge(graph, quadNode, p, quad.getPredicate(), nodeSupplier, varToNode);
        addEdge(graph, quadNode, o, quad.getObject(), nodeSupplier, varToNode);

        return quadNode;
    }


    /**
     * Connects every quad's node to a newly allocated node representing the quad block
     *
     * @param graph
     * @param quads
     * @return
     */
    public static Node quadsToGraphNode(Graph graph, Collection<Quad> quads, Supplier<Node> nodeSupplier, Map<Var, Node> varToNode) {
        Node quadBlockNode = nodeSupplier.get();
        //graph = new DirectedPseudograph<>(LabeledEdgeImpl.class);
        for(Quad quad : quads) {
            Node quadNode = addQuad(graph, quad, nodeSupplier, varToNode);
            addEdge(graph, quadBlockNode, quadBlockMember, quadNode, nodeSupplier, varToNode);
        }

        return quadBlockNode;
    }


    public static void quadsToGraph(Graph graph, Collection<Quad> quads, Supplier<Node> nodeSupplier, Map<Var, Node> varToNode) {
        //graph = new DirectedPseudograph<>(LabeledEdgeImpl.class);
        for(Quad quad : quads) {
            addQuad(graph, quad, nodeSupplier, varToNode);
        }
    }

    // Filters: Extract all equality filters
    public static void equalExprsToGraph(Graph graph, Collection<? extends Collection<? extends Expr>> dnf, Supplier<Node> nodeSupplier, Map<Var, Node> varToNode) {
        Set<Map<Var, NodeValue>> maps = DnfUtils.extractConstantConstraints(dnf);

        for(Map<Var, NodeValue> map : maps) {
            // Create a blank node for each clause
            Node orNode = nodeSupplier.get();

            for(Entry<Var, NodeValue> e : map.entrySet()) {
                // Create another blank node for each equality instance
                // TODO This would be another type of construction: Actually the edge labels are already sufficient for discrimination of equals expressions
                boolean createNodesForEqualities = true; // Does not work with false as search space gets too big
                if(createNodesForEqualities) {
                    Node equalsNode = nodeSupplier.get();

                    Var v = e.getKey();
                    Node c = e.getValue().getNode();

                    addEdge(graph, equalsNode, ev, v, nodeSupplier, varToNode);
                    addEdge(graph, c, ec, equalsNode, nodeSupplier, varToNode);
                    addEdge(graph, orNode, dm, equalsNode, nodeSupplier, varToNode);

                } else {

                    Var v = e.getKey();
                    Node c = e.getValue().getNode();

                    addEdge(graph, orNode, Vars.x, v, nodeSupplier, varToNode);
                    addEdge(graph, c, Vars.y, orNode, nodeSupplier, varToNode);
                }
            }
        }
    }


    public static Stream<BiMap<Node, Node>> match(Graph a, Graph b) {
        DirectedGraph<Node, Triple> adg = new PseudoGraphJenaGraph(a);
        DirectedGraph<Node, Triple> bdg = new PseudoGraphJenaGraph(b);

        Stream<BiMap<Node, Node>> result = match(adg, bdg);
        return result;
    }


    public static Stream<Map<Var, Var>> match(Graph a, Map<Node, Var> aNodeToVar, Graph b, Map<Node, Var> bNodeToVar) {
        DirectedGraph<Node, Triple> adg = new PseudoGraphJenaGraph(a);
        DirectedGraph<Node, Triple> bdg = new PseudoGraphJenaGraph(b);

        Stream<Map<Var, Var>> result = matchOld(adg, aNodeToVar, bdg, bNodeToVar);
        return result;
    }


    public static Stream<BiMap<Node, Node>> match(
            DirectedGraph<Node, Triple> a,
            DirectedGraph<Node, Triple> b) {

//        System.out.println("EDGES:");
//        b.edgeSet().forEach(System.out::println);
//        System.out.println("done with edges");

        Comparator<Node> nodeCmp = (x, y) -> {
            int  r = (x.isVariable() && y.isVariable()) || (x.isBlank() && y.isBlank())
                    ? 0
                    : x.toString().compareTo(y.toString());
            //System.err.println("NodeCmp [" + r + "] for " + x + " <-> " + y);
            return r;
        };

        Comparator<Triple> edgeCmp = (x, y) -> {
            int r = x.getPredicate().toString().compareTo(y.getPredicate().toString());
            //System.err.println("EdgeCmp: [" + r + "] for " + x + " <-> " + y);
            return r;
        };



        VF2SubgraphIsomorphismInspector<Node, Triple> inspector = new VF2SubgraphIsomorphismInspector<>(a, b, nodeCmp, edgeCmp, true);
        Iterator<GraphMapping<Node, Triple>> it = inspector.getMappings();

        Stream<BiMap<Node, Node>> result = StreamUtils.stream(it)
                .map(m -> (IsomorphicGraphMapping<Node, Triple>)m)
                .map(m -> {
                    BiMap<Node, Node> nodeMap = HashBiMap.create();//new HashMap<>();
                    for(Node aNode : a.vertexSet()) {
                        if(m.hasVertexCorrespondence(aNode)) {
                            Node bNode = m.getVertexCorrespondence(aNode, true);
                            nodeMap.put(aNode, bNode);
                        }
                    }


                    //System.out.println("Mapping: " + m);
//                    Map<Var, Var> varMap = null;
//                    for(Node bNode : b.vertexSet()) {
//                        if(bNode.isVariable()) {
//                            if(m.hasVertexCorrespondence(bNode)) {
//                                Node aNode = m.getVertexCorrespondence(bNode, true);
//                                if(aNode.isVariable()) {
//                                    varMap = varMap == null ? new HashMap<>() : varMap;
//                                    varMap.put((Var)bNode, (Var)aNode);
//                                } else {
//                                    break;
//                                }
//                            }
//                        }
//                    }

                    return nodeMap;
                }).
                filter(x -> x != null);

        return result;
    }

    public static Stream<Map<Var, Var>> matchOld(
            DirectedGraph<Node, Triple> a,
            Map<Node, Var> aNodeToVar,
            DirectedGraph<Node, Triple> b,
            Map<Node, Var> bNodeToVar) {

//        System.out.println("EDGES:");
//        b.edgeSet().forEach(System.out::println);
//        System.out.println("done with edges");

        Comparator<Node> nodeCmp = (x, y) -> {
            int  r = (x.isVariable() && y.isVariable()) || (x.isBlank() && y.isBlank())
                    ? 0
                    : x.toString().compareTo(y.toString());
            //System.err.println("NodeCmp [" + r + "] for " + x + " <-> " + y);
            return r;
        };

        Comparator<Triple> edgeCmp = (x, y) -> {
            int r = x.getPredicate().toString().compareTo(y.getPredicate().toString());
            //System.err.println("EdgeCmp: [" + r + "] for " + x + " <-> " + y);
            return r;
        };



        VF2SubgraphIsomorphismInspector<Node, Triple> inspector = new VF2SubgraphIsomorphismInspector<>(b, a, nodeCmp, edgeCmp, true);
        Iterator<GraphMapping<Node, Triple>> it = inspector.getMappings();

        Stream<Map<Var, Var>> result = StreamUtils.stream(it)
                .map(m -> (IsomorphicGraphMapping<Node, Triple>)m)
                .map(m -> {
                    //System.out.println("Mapping: " + m);
                    Map<Var, Var> varMap = null;
                    for(Node bNode : b.vertexSet()) {
                        Var bVar = bNodeToVar.get(bNode);
                        if(bVar != null) {
                            if(m.hasVertexCorrespondence(bNode)) {
                                Node aNode = m.getVertexCorrespondence(bNode, true);
                                Var aVar = aNodeToVar.get(aNode);
                                if(aVar != null) {
                                    varMap = varMap == null ? new HashMap<>() : varMap;
                                    varMap.put(bVar, aVar);
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    return varMap;
                }).
                filter(x -> x != null);

        return result;
    }

}

