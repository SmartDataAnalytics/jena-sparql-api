package org.aksw.jena_sparql_api.sparql_path.core;

import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.graph.Graph;

public interface ConceptPathFinder {
	ConceptPathFinder setDataSummary(Graph dataSummary);
	ConceptPathFinder setSourceConcept(UnaryRelation concept);
	ConceptPathFinder setTargetConcept(UnaryRelation concept);
	
	void findPaths();
}
