package org.aksw.jena_sparql_api.sparql_path.api;


import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;

public interface ConceptPathFinder {
	PathSearch<Path> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept);
	//ConceptPathFinder setSource(UnaryRelation source);
	//ConceptPathFinder setTarget(UnaryRelation target);
}