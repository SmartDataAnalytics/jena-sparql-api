package org.aksw.jena_sparql_api.shape.algebra.op;

public interface OpVisitor<T> {
    T visit(OpTop op);
    T visit(OpEnumeration op);
    T visit(OpType op);
    T visit(OpFocus op);
    T visit(OpConcept op);
    T visit(OpFilter op);
    T visit(OpSparqlConcept op);
    T visit(OpAssign op);
    T visit(OpAnd op);
    T visit(OpUnion op);
    T visit(OpExists op);
    T visit(OpForAll op);
}
