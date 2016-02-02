package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Set;

import org.jgrapht.DirectedGraph;

public interface Nfa<V, E> {
    DirectedGraph<V, E> getGraph();
    Set<V> getStartStates();
    Set<V> getEndStates();
}