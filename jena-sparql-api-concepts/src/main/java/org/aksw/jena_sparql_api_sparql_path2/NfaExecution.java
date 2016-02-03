package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class NfaExecution<V> {
    protected Nfa<V, LabeledEdge<V, Path>> nfa;
    protected Set<V> currentStates;
    protected QueryExecutionFactory qef;

    // Mapping from state to each vertex encountered at that node
    //protected Map<V, Frontier<Node>> seen = new HashMap<V, Frontier<Node>>();

    //protected Multimap<V, RdfPath> paths = ArrayListMultimap.create();

    // for each state, for each node, store all paths that end with that node
    // i.e.: for every path keep track at which state and node it is in
    protected Map<V, Multimap<Node, NestedRdfPath>> paths = new HashMap<V, Multimap<Node, NestedRdfPath>>();

    protected Set<NestedRdfPath> accepted = new HashSet<NestedRdfPath>();


    //protected Map<List<RdfPath> frontier = new ArrayList<RdfPath>();

    public NfaExecution(Nfa<V, LabeledEdge<V, Path>> nfa, QueryExecutionFactory qef) {
        this.nfa = nfa;
        this.qef = qef;

        //Predicate

        for(V v :nfa.getGraph().vertexSet()) {
            //seen.put(v, new FrontierImpl<Node>());
            paths.put(v, ArrayListMultimap.create());
        }

        this.currentStates = new HashSet<V>(nfa.getStartStates());
    }

    public void add(Node node) {
        for(V state : currentStates) {
//            Frontier<Node> frontier = seen.get(state);
//            frontier.add(node);

            NestedRdfPath rdfPath = new NestedRdfPath(node);
            paths.get(state).put(node, rdfPath);
        }
    }


    /**
     * advances the state of the execution. returns false to indicate finished execution
     * @return
     *
     * TODO: We should detect dead states, as to prevent potential cycling in them indefinitely
     */
    public boolean advance() {
        boolean result = false;

        // Check for all paths that
        for(V state : currentStates) {
            boolean isFinal = isFinalState(state);
            if(isFinal) {
                Multimap<Node, NestedRdfPath> ps = paths.get(state);
                for(NestedRdfPath path : ps.values()) {
                    // skip paths that have already been accepted
                    if(!accepted.contains(path)) {
                        accepted.add(path);
                        //System.out.println("ACCEPTED: " + path.asSimplePath().getLength() + ": " + path.asSimplePath().getEnd());
                        System.out.println("ACCEPTED" + path.asSimplePath());
                    }

                }
            }
        }

        Set<V> nextCurrentStates = new HashSet<V>();

        for(V state : currentStates) {
            DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();
            Set<LabeledEdge<V, Path>> transitions = JGraphTUtils.resolveTransitions(graph, state);

            //Set success
            //JGraphTUtils.transitiveGet(graph, startVertex, 1, edge -> edge.getLabel() == null);

            Multimap<Node, NestedRdfPath> ps = paths.get(state);
            Set<Node> nodes = ps.keySet();
            //Frontier<Node> frontier = seen.get(state);

//            Set<Node> nodes = new HashSet<Node>();
//            while(!frontier.isEmpty()) {
//                nodes.add(frontier.next());
//            }

            Concept filter = ConceptUtils.createFilterConcept(nodes);

            // Check if the state is an accepting state, if so, yield all paths
            // that made it to this node
            boolean isFinal = isFinalState(state);
            if(isFinal) {
                System.out.println("GOT " + paths.get(state));
            }


            Multimap<Node, NestedRdfPath> mm = HashMultimap.create();

            for(LabeledEdge<V, Path> transition : transitions) {

                Path path = transition.getLabel();

//                PathVisitorPredicateClass predicateClassVisitor = new PathVisitorPredicateClass();
//                path.visit(predicateClassVisitor);
//                PredicateClass predicateClass = predicateClassVisitor.getResult();


                PathVisitorResourceShapeBuilder visitor = new PathVisitorResourceShapeBuilder();
                path.visit(visitor);
                ResourceShapeBuilder rsb = visitor.getResourceShapeBuilder();


                MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), filter);
                System.out.println("MC: " + mc);

                ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceAcc(qef, mc, false);

                Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);
                //System.out.println(nodeToGraph);

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
                                result = true;
                                // TODO Properly remove paths from the old state

                                // Get the set of successor states for the given predicates
                                V targetState = transition.getTarget();
                                mm.put(next.getCurrent(), next);
                                nextCurrentStates.add(targetState);
                            }
                        }

                        //ps.clear();
                        //paths.get(targetState).put(next.getCurrent(), next);

                    }
                }
            }

            //ps = paths.get(state);

            ps.clear();
            ps.putAll(mm);
        }

        currentStates = nextCurrentStates;
        return result;
    }

    /**
     * Tests if a state is final. This includes if there is a transitive
     * connection via epsilon edges to a final state.
     *
     * @param state
     * @return
     */
    public boolean isFinalState(V state) {
        DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();
        Set<V> endStates = nfa.getEndStates();
        Set<V> reachableStates = JGraphTUtils.transitiveGet(graph, state, 1, x -> x.getLabel() == null);
        boolean result = reachableStates.stream().anyMatch(s -> endStates.contains(s));
        return result;
    }

    /**
     * Get transitions, thereby resolve epsilon edges
     *
     * TODO Shoud we return a Multimap<V, E> or a Graph<V, E> ???
     *
     */
    public Multimap<V, LabeledEdge<V, Path>> getTransitions() {
        Multimap<V, LabeledEdge<V, Path>> result = ArrayListMultimap.<V, LabeledEdge<V,Path>>create();

        DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();

        for(V state : currentStates) {
            Set<LabeledEdge<V, Path>> edges = JGraphTUtils.resolveTransitions(graph, state);
            result.putAll(state, edges);

        }
        return result;
    }


    /**
     * Map each current state to the set of corresponding transitions
     * This method resolves epsilon edges.
     *
     * @return
     */
//    protected Graph<V, E> getTransitions() {
//
//    }
}