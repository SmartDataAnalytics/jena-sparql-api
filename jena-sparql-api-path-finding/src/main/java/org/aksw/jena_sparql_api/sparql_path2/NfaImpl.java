package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.EdgeReversedGraph;

public class NfaImpl<V, E>
    implements Nfa<V, E>
{
    protected Graph<V, E> graph;
    protected Set<V> startStates;
    protected Set<V> endStates;

    public NfaImpl(Graph<V, E> graph, Set<V> startStates,
            Set<V> endStates) {
        super();
        this.graph = graph;
        this.startStates = startStates;
        this.endStates = endStates;
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public Set<V> getStartStates() {
        return startStates;
    }

    public Set<V> getEndStates() {
        return endStates;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((endStates == null) ? 0 : endStates.hashCode());
        result = prime * result + ((graph == null) ? 0 : graph.hashCode());
        result = prime * result
                + ((startStates == null) ? 0 : startStates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NfaImpl other = (NfaImpl) obj;
        if (endStates == null) {
            if (other.endStates != null)
                return false;
        } else if (!endStates.equals(other.endStates))
            return false;
        if (graph == null) {
            if (other.graph != null)
                return false;
        } else if (!graph.equals(other.graph))
            return false;
        if (startStates == null) {
            if (other.startStates != null)
                return false;
        } else if (!startStates.equals(other.startStates))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NfaImpl [graph=" + graph + ", startStates=" + startStates
                + ", endStates=" + endStates + "]";
    }

    /**
     * IMPORTANT: Only provides an edge reverse view, does not change the edges themselves
     * @param nfa
     * @return
     */
    public static <V, E> Nfa<V, E> reverse(Nfa<V, E> nfa) {
        EdgeReversedGraph<V, E> g = new EdgeReversedGraph<>(nfa.getGraph());
        NfaImpl<V, E> result = new NfaImpl<>(g, nfa.getEndStates(), nfa.getStartStates());
        return result;
    }

//    public static <V, E, I> Nfa<V, I> invert(Graph<V, I> invGraph, Graph<V, E> graph, Function<E, I> edgeInvertFn) {
//        //Graph<V, E> graph = nfa.getGraph();
//
//        //Graph<V, I> invGraph = new SimpleGraph<V, I>();
//
//
//        for(V sourceVertex : graph.vertexSet()) {
//            invGraph.addVertex(sourceVertex);
//        }
//
//        Set<E> edges = graph.edgeSet();
//        for(E edge : edges) {
//            V sourceVertex = graph.getEdgeSource(edge);
//            V targetVertex = graph.getEdgeTarget(edge);
//            I invEdge = edgeInvertFn.apply(edge);
//
//            invGraph.addEdge(targetVertex, sourceVertex, invEdge);
//        }
//
//        return null;
//    }


}
