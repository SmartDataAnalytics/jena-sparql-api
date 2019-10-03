package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefVisitor<T> {
	T visit(DataRefFromUrl dataRef);
	T visit(DataRefFromCatalog dataRef);
	T visit(DataRefFromSparqlEndpoint dataRef);
	T visit(DataRefExt dataRef);
}
