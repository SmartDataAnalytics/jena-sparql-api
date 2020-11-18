package org.aksw.jena_sparql_api.concept.parser;

import org.aksw.jena_sparql_api.concepts.Concept;

import org.apache.jena.query.Query;

public interface PartQueryFactory {
    Query createQuery(Concept concept);
}
