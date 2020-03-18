package org.aksw.jena_sparql_api.conjure.dataset.algebra;

public interface OpVisitor<T> {
	T visit(OpData op);
	T visit(OpDataRefResource op);
	T visit(OpConstruct op);
	T visit(OpUpdateRequest op);
	T visit(OpStmtList op);
	T visit(OpUnion op);
	T visit(OpCoalesce op);
	T visit(OpPersist op);

	T visit(OpHdtHeader op);


	T visit(OpMacroCall op);
	T visit(OpSequence op);
	T visit(OpSet op);
	T visit(OpWhen op);
	T visit(OpError op);


	T visit(OpVar op);
	T visit(OpJobInstance op);
	
	T visit(OpQueryOverViews op);
}
