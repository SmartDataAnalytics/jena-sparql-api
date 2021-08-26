package org.aksw.jena_sparql_api.rx;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ModelFactoryEx {
    public static Model createInsertOrderPreservingModel() {
        Graph graph = GraphFactoryEx.createInsertOrderPreservingGraph();
        return ModelFactory.createModelForGraph(graph);
    }
}
