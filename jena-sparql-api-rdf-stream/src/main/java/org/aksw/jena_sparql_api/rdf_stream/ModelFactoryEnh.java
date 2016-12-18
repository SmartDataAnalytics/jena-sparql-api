package org.aksw.jena_sparql_api.rdf_stream;

import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.sparql.graph.GraphFactory;

public class ModelFactoryEnh {
    public static Model createModel() {
    	Personality<RDFNode> personality = BuiltinPersonalities.model
    			.copy()
    			.add(ResourceEnh.class, new ImplementationEnh());

    	Graph graph = GraphFactory.createDefaultGraph();
		Model result = new ModelCom(graph, personality);

		return result;
    }
}
