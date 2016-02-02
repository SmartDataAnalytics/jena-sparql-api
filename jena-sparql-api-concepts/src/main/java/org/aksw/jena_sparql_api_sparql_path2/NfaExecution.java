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
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class NfaExecution<V> {
    protected Nfa<V, LabeledEdge<V, Path>> nfa;
    protected Set<V> currentStates;
    protected QueryExecutionFactory qef;

    // Mapping from state to each vertex encountered at that node
    protected Map<V, Frontier<Node>> seen = new HashMap<V, Frontier<Node>>();

    protected Multimap<V, RdfPath> paths = ArrayListMultimap.create();


    //protected Map<List<RdfPath> frontier = new ArrayList<RdfPath>();

    public NfaExecution(Nfa<V, LabeledEdge<V, Path>> nfa, QueryExecutionFactory qef) {
        this.nfa = nfa;
        this.qef = qef;


        for(V v :nfa.getGraph().vertexSet()) {
            seen.put(v, new FrontierImpl<Node>());
        }

        this.currentStates = new HashSet<V>(nfa.getStartStates());
    }

    public void add(Node node) {
        for(V state : currentStates) {
            Frontier<Node> frontier = seen.get(state);
            frontier.add(node);
        }
    }


    public void advance() {
        for(V state : currentStates) {
            DirectedGraph<V, LabeledEdge<V, Path>> graph = nfa.getGraph();
            Set<LabeledEdge<V, Path>> transitions = JGraphTUtils.resolveTransitions(graph, state);

            Frontier<Node> frontier = seen.get(state);

            Set<Node> nodes = new HashSet<Node>();
            while(!frontier.isEmpty()) {
                nodes.add(frontier.next());
            }

            Concept filter = ConceptUtils.createFilterConcept(nodes);

            // Check if the state is an accepting state, if so, yield all paths
            // that made it to this node
            boolean isFinal = isFinalState(state);
            if(isFinal) {
                System.out.println("GOT " + paths.get(state));
            }


            for(LabeledEdge<V, Path> transition : transitions) {

                PathVisitorResourceShapeBuilder visitor = new PathVisitorResourceShapeBuilder();

                Path path = transition.getLabel();

                path.visit(visitor);

                ResourceShapeBuilder rsb = visitor.getResourceShapeBuilder();
                MappedConcept<Graph> mc = ResourceShape.createMappedConcept(rsb.getResourceShape(), filter);
                System.out.println("MC: " + mc);

                ListService<Concept, Node, Graph> ls = ListServiceUtils.createListServiceAcc(qef, mc, false);

                Map<Node, Graph> nodeToGraph = ls.fetchData(null, null, null);
                //System.out.println(nodeToGraph);

                for(Entry<Node, Graph> entry : nodeToGraph.entrySet()) {

                }

            }

        }
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