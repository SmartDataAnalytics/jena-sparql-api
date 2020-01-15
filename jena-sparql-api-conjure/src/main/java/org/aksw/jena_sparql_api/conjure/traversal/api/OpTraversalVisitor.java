package org.aksw.jena_sparql_api.conjure.traversal.api;

public interface OpTraversalVisitor<T> {
	T visit(OpPropertyPath op);
	T visit(OpTraversalSelf op);
}
