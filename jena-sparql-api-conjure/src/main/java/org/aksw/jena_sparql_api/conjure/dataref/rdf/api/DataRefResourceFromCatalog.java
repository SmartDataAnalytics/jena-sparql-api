package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface DataRefResourceFromCatalog
	extends DataRefFromCatalog, DataRefResource
{
	@Override
	@IriNs("eg")
	DataRefResource getCatalogDataRef();
	DataRefResourceFromCatalog setCatalogDataRef(DataRefResource dataRef);
	
	@IriNs("eg")
	DataRefResourceFromCatalog setEntryId(String entryId);
	
	@Override
	default <T> T accept2(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
