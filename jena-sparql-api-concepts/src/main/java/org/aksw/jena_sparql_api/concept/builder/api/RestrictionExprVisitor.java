package org.aksw.jena_sparql_api.concept.builder.api;

public interface RestrictionExprVisitor<T> {
    T visit(RestrictionExprExists re);
    T visit(RestrictionExprForAll re);
    T visit(RestrictionExprExt re);
}
