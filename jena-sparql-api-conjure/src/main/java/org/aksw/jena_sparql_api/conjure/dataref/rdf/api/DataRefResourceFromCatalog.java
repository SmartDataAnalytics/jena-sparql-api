package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefResource;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface DataRefResourceFromCatalog
	extends DataRefResource, DataRefFromCatalog
{
	@Override
	@IriNs("eg")
	DataRefResource getCatalogDataRef();
	DataRefResourceFromCatalog setCatalogDataRef(DataRefResource dataRef);
	
	@IriNs("eg")
	DataRefResourceFromCatalog setEntryId(String entryId);
	
	default <T> T accept(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
