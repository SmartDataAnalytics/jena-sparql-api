package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public interface PathVisitor<T> {
    T visit(VPath<?> path);
}
