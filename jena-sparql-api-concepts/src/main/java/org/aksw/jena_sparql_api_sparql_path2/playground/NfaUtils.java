package org.aksw.jena_sparql_api_sparql_path2.playground;

import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.NfaImpl;
import org.jgrapht.Graph;
import org.jgrapht.graph.EdgeReversedGraph;

public class NfaUtils {

    public static <S, T> Nfa<S, T> reverse(Nfa<S, T> nfa) {
        Graph<S, T> reverseGraph = new EdgeReversedGraph<>(nfa.getGraph());
        Nfa<S, T> result = new NfaImpl<>(reverseGraph, nfa.getEndStates(), nfa.getStartStates());
        return result;
    }

    /**
     * The nfa is positive, if there is no edge with a negated property set
     */
//    boolean <S, T> isPositive(Nfa<S, T> nfa, Function<T, PredicateClass> ) {
//
//    }
}
