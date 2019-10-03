package org.aksw.jena_sparql_api.conjure.dataref.core.api;

public interface DataRefFromCatalog
	extends DataRef
{
	DataRef getCatalogDataRef();
	String getEntryId();
}
