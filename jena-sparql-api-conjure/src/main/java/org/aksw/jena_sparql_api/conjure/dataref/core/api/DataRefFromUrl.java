package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefFromUrl
	extends DataRef
{
	String getDataRefUrl();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
}
