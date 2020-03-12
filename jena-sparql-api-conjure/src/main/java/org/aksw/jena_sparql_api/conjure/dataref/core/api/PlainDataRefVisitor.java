package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface PlainDataRefVisitor<T> {
//	T visit(DataRefEmpty dataRef);
	
	T visit(PlainDataRefUrl dataRef);
	T visit(PlainDataRefCatalog dataRef);
	T visit(PlainDataRefSparqlEndpoint dataRef);
	T visit(PlainDataRefOp dataRef);

	T visit(PlainDataRefDcat dataRef);
	T visit(PlainDataRefGit dataRef);

	T visit(PlainDataRefExt dataRef);	
}
