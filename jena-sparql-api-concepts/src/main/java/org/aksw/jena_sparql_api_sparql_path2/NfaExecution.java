package org.aksw.jena_sparql_api_sparql_path2;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.path.Path;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class NfaExecution<V> {
    protected Nfa<V, LabeledEdge<V, Path>> nfa;
    protected Set<V> currentStates;
    protected QueryExecutionFactory qef


    public NfaExecution(Nfa<V, LabeledEdge<V, Path>> nfa, QueryExecutionFactory qef) {
        this.nfa = nfa;
        this.qef = qef;

        this.currentStates = new HashSet<V>(nfa.getStartStates());
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