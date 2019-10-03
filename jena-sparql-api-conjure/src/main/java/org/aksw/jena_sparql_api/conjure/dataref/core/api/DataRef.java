package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRef {
	<T> T accept(DataRefVisitor<T> visitor);
}
