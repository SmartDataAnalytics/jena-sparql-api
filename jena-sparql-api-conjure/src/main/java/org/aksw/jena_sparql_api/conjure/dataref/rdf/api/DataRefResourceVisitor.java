package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

public interface DataRefResourceVisitor<T>
{
	T visit(DataRefResourceFromUrl dataRef);
	T visit(DataRefResourceFromCatalog dataRef);
	T visit(DataRefResourceFromSparqlEndpoint dataRef);
	T visit(DataRefResourceExt dataRef);
}
