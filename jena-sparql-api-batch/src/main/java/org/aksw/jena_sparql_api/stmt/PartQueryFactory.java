package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.concepts.Concept;

import com.hp.hpl.jena.query.Query;

public interface PartQueryFactory {
    Query createQuery(Concept concept);
}
