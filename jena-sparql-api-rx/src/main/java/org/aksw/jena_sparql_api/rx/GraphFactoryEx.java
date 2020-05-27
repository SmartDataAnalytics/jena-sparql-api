package org.aksw.jena_sparql_api.rx;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraph;

public class GraphFactoryEx {
    public static Graph createInsertOrderPreservingGraph() {
        DatasetGraph tmp = DatasetGraphFactoryEx.createInsertOrderPreservingDatasetGraph();
        Graph result = tmp.getDefaultGraph();
        return result;
    }
}
