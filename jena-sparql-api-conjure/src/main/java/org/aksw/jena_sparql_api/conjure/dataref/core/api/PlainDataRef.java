package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface PlainDataRef {
	<T> T accept(PlainDataRefVisitor<T> visitor);
}
