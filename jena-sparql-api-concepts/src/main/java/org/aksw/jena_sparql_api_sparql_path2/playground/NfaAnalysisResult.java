package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;

import org.aksw.jena_sparql_api.utils.Pair;
import org.apache.jena.graph.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class NfaAnalysisResult<S> {
    public Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost;
    public Graph<Node, DefaultEdge> joinGraph;

    public NfaAnalysisResult(
            Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost,
            Graph<Node, DefaultEdge> joinGraph) {
        super();
        this.stateToDiPredToCost = stateToDiPredToCost;
        this.joinGraph = joinGraph;
    }


}
