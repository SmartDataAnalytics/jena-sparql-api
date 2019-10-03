package org.aksw.jena_sparql_api.conjure.dataref.api;

public interface DataRef {
	<T> T accept(DataRefVisitor<T> visitor);
}
