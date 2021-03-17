package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

/**
 *
 *
 *
 * @author raven
 *
 */
public class NfaSegmenter {


    /**
     *
     *
     *
     * @param nfa
     * @return
     */
    public <S, T> Set<T> segment(Nfa<S, T> nfa) {
        Graph<S, T> graph = nfa.getGraph();



        ConnectivityInspector<S, T> ci = new ConnectivityInspector<>(graph);


        List<Set<S>> connectedSets = ci.connectedSets();
        if(connectedSets.size() == 2) {
            // Check whether start and end states are separated
            boolean isSegmented =
                    connectedSets.get(0).containsAll(nfa.getStartStates()) && connectedSets.get(1).containsAll(nfa.getEndStates()) ||
                    connectedSets.get(0).containsAll(nfa.getEndStates()) && connectedSets.get(1).containsAll(nfa.getStartStates());


        }
return null;
    }
}
