package org.aksw.jena_sparql_api.sparql_path.api;


import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;

public interface ConceptPathFinder {
	PathSearch<SimplePath> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept);
	//ConceptPathFinder setSource(UnaryRelation source);
	//ConceptPathFinder setTarget(UnaryRelation target);
}