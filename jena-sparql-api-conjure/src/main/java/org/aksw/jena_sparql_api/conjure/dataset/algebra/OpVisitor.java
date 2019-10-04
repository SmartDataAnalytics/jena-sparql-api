package org.aksw.jena_sparql_api.conjure.dataset.algebra;

public interface OpVisitor<T> {
	T visit(OpData op);
	T visit(OpDataRefResource op);
	T visit(OpConstruct op);
	T visit(OpUpdateRequest op);
	T visit(OpUnion op);
	T visit(OpPersist op);

	T visit(OpVar op);
}
