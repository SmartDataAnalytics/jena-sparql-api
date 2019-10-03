package org.aksw.jena_sparql_api.conjure.dataref.api;

public interface DataRefFromUrl
	extends DataRef
{
	//Path getPath();
	String getUrl();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
