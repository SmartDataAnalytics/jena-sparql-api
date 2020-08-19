package org.aksw.jena_sparql_api.shape.algebra.op;

public interface Op {
    <T> T accept(OpVisitor<T> visitor);
}
