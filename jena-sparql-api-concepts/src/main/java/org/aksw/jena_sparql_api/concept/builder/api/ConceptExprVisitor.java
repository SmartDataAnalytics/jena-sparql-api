package org.aksw.jena_sparql_api.concept.builder.api;

public interface ConceptExprVisitor<T> {
    T visit(ConceptExprConcept ce);
    T visit(ConceptExprConceptBuilder ce);
    T visit(ConceptExprList ce);
    T visit(ConceptExprExt cse);
}
