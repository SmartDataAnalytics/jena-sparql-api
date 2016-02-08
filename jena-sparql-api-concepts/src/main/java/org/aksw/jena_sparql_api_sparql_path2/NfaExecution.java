package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.Multimap;



public class NfaExecution<V> {
    protected Nfa<V, LabeledEdge<V, Path>> nfa;
    protected QueryExecutionFactory qef;

    /**
     * The frontier keeps track of the current paths being traced
     */
    protected Frontier<V> frontier;

    protected Set<NestedRdfPath> accepted = new HashSet<NestedRdfPath>();
    protected Function<RdfPath, Boolean> pathCallback;

    /**
     * If an nfa is reversed only be reversing the edges of the automaton, the edge labels themselves
     * are not reversed. This flag is used to treat edge labels (i.e. property directions) reversed.
     */
    protected boolean reversePropertyDirection = false;


    public NfaExecution(Nfa<V, LabeledEdge<V, Path>> nfa, QueryExecutionFactory qef, boolean reversePropertyDirection, Function<RdfPath, Boolean> pathCallback) {
        this.nfa = nfa;
        this.qef = qef;
        this.reversePropertyDirection = reversePropertyDirection;
        this.pathCallback = pathCallback;

        this.frontier = new Frontier<V>();
    }

    /**
     * Adds a node to the frontier under the given states
     *
     * @param states
     * @param node
     */
    public void add(Set<V> states, Node node) {
        for(V state : states) {
            NestedRdfPath rdfPath = new NestedRdfPath(node);
            frontier.add(state, rdfPath);
        }
    }


    public static <V> boolean collectPaths(Nfa<V, LabeledEdge<V, Path>> nfa, Frontier<V> frontier, Function<RdfPath, Boolean> pathCallback) {
        boolean isFinished = false;
        Set<V> currentStates = frontier.getCurrentStates();
        for(V state : currentStates) {

            boolean isFinal = isFinalState(nfa, state);
            if(isFinal) {
                Multimap<Node, NestedRdfPath> ps = frontier.getPaths(state);
                for(NestedRdfPath path : ps.values()) {
                    isFinished = pathCallback.apply(path.asSimplePath());
                    if(isFinished) {
                        break;
                    }
                }
            }

            if(isFinished) {
                break;
            }
        }

        return isFinished;
    }


    /**
     * advances the state of the execution. returns false to indicate finished execution
     * @return
     *
     * TODO: We should detect dead states, as to prevent potential cycling in them indefinitely
     */
    public boolean advance() {
        boolean isFinished = collectPaths(nfa, frontier, pathCallback);
        boolean result;

        if(isFinished) {
            result = false;
        } else {
            frontier = advanceFrontier(frontier, nfa, qef, reversePropertyDirection);
            result = !frontier.isEmpty();
        }

        return result;
    }


    public static <V> Frontier<V> advanceFrontier(Frontier<V> frontier, Nfa<V, LabeledEdge<V, Path>> nfa, QueryExecutionFactory qef, boolean reversePropertyDirection) {
        // Prepare the next frontier
        Frontier<V> result = new Frontier<V>();

        Set<V> currentStates = frontier.getCurrentStates();
        for(V state : currentStates) {
            Multimap<Node, NestedRdfPath> ps = frontier.getPaths(state);

            DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();
            Set<LabeledEdge<V, Path>> transitions = JGraphTUtils.resolveTransitions(graph, state);


            Set<Node> nodes = ps.keySet();
            Concept filter = ConceptUtils.createFilterConcept(nodes);

            for(LabeledEdge<V, Path> transition : transitions) {

                Path path = transition.getLabel();

                PathVisitorResourceShapeBuilder visitor = new PathVisitorResourceShapeBuilder(reversePropertyDirection);
                path.visit(visitor);
                ResourceShapeBuilder rsb = visitor.getResourceShapeBuilder();


                MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), filter);
                //System.out.println("MC: " + mc);
                ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceAcc(qef, mc, false);

                Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);

                for(Entry<Node, Graph> entry : nodeToGraph.entrySet()) {
                    Node node = entry.getKey();
                    Graph g = entry.getValue();

                    for(Triple t : g.find(Node.ANY, Node.ANY, Node.ANY).toSet()) {
                        Node p = t.getPredicate();
                        P_Path0 p0;

                        Node o;
                        if(t.getSubject().equals(node)) {
                            p0 = new P_Link(p);
                            o = t.getObject();
                        } else if(t.getObject().equals(node)) {
                            p0 = new P_ReverseLink(p);
                            //t = TripleUtils.swap(t);
                            o = t.getSubject();
                        } else {
                            throw new RuntimeException("Should not happen");
                        }

                        for(NestedRdfPath parentPath : ps.values()) {
                            NestedRdfPath next = new NestedRdfPath(parentPath, p0, o);

                            if(next.isCycleFree()) {
                                V targetState = transition.getTarget();
                                result.add(targetState, next);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Tests if a state is final. This includes if there is a transitive
     * connection via epsilon edges to a final state.
     *
     * @param state
     * @return
     */
    public static <V> boolean isFinalState(Nfa<V, LabeledEdge<V, Path>> nfa, V state) {
        DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();
        Set<V> endStates = nfa.getEndStates();
        Set<V> reachableStates = JGraphTUtils.transitiveGet(graph, state, 1, x -> x.getLabel() == null);
        boolean result = reachableStates.stream().anyMatch(s -> endStates.contains(s));
        return result;
    }

}