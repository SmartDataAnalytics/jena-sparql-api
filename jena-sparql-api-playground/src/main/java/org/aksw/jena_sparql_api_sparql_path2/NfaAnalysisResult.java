package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class NfaAnalysisResult<S> {
    public Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost;
    public DirectedGraph<Node, DefaultEdge> joinGraph;

    public NfaAnalysisResult(
            Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost,
            DirectedGraph<Node, DefaultEdge> joinGraph) {
        super();
        this.stateToDiPredToCost = stateToDiPredToCost;
        this.joinGraph = joinGraph;
    }


}
