package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefUrl
	extends PlainDataRefUrl, DataRef
{
	@IriNs("eg")
	@IriType
	DataRefUrl setDataRefUrl(String url);
	
	// Experimental feature adding hdt header as a first class modifier for a dataset reference
	@IriNs("eg")
	DataRefUrl hdtHeader(Boolean value);

	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefUrl create(String url) {
		DataRefUrl result = ModelFactory.createDefaultModel().createResource().as(DataRefUrl.class)
				.setDataRefUrl(url);
		return result;
	}
}
