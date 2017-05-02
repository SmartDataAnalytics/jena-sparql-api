package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public interface PathVisitorArg<T, A> {
    T visit(VPath<?> e, A arg);
}
