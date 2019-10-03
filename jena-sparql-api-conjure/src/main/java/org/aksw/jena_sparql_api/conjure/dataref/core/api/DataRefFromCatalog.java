package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefFromCatalog
	extends DataRef
{
	DataRef getCatalogDataRef();
	String getEntryId();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
