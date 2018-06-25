package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Set;

import org.jgrapht.Graph;

public interface Nfa<V, E> {
    Graph<V, E> getGraph();
    Set<V> getStartStates();
    Set<V> getEndStates();

    /**
     * States created by the factory are not automatically added the NFA's graph.
     *
     * @return
     */
    //VertexFactory<V> getStateFactory();

    //boolean isEpsilon(E edge);
}