package org.aksw.jena_sparql_api.conjure.entity.algebra;

public interface OpVisitor<T> {
	T visit(OpCode op);
	T visit(OpConvert op);
	T visit(OpValue op);
	T visit(OpPath op);
}