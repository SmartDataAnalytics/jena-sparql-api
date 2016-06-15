package org.aksw.jena_sparql_api.concept.builder.api;

public interface RestrictionExpr {
    <T> T accept(RestrictionExprVisitor<T> visitor);
}
