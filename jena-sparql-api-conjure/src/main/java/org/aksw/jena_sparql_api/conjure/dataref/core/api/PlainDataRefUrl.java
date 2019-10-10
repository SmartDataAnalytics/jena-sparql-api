package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface PlainDataRefUrl
	extends PlainDataRef
{
	String getDataRefUrl();
	Boolean hdtHeader();
	
	@Override
	default <T> T accept(PlainDataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}	
}
