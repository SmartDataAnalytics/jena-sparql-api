package org.aksw.jena_sparql_api.conjure.dataref.api;

public interface DataRefVisitor<T> {
	T visit(DataRefFromUrl dataRef);
	T visit(DataRefFromEntity dataRef);
	T visit(DataRefFromRemoteSparqlDataset dataRef);
	T visit(DataRefFromRDFConnection dataRef);
	T visit(DataRefExt dataRef);
}
