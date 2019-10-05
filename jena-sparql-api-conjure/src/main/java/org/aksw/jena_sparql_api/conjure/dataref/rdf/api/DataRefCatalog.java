package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefCatalog;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface DataRefCatalog
	extends PlainDataRefCatalog, DataRef
{
	@Override
	@IriNs("eg")
	DataRef getCatalogDataRef();
	DataRefCatalog setCatalogDataRef(DataRef dataRef);
	
	@IriNs("eg")
	DataRefCatalog setEntryId(String entryId);
	
	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
