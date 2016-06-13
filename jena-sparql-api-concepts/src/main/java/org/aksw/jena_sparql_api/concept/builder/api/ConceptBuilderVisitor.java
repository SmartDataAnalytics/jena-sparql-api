package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderAnd;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderImpl;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderUnion;

public interface ConceptBuilderVisitor<T> {
    T visit(ConceptBuilderImpl cb);
    T visit(ConceptBuilderAnd cb);
    T visit(ConceptBuilderUnion cb);
    T visit(ConceptBuilderExt cb);
}
