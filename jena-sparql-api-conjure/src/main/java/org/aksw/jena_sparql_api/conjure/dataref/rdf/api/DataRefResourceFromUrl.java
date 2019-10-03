package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefResourceFromUrl
	extends DataRefFromUrl, DataRefResource
{
	@IriNs("eg")
	DataRefResourceFromUrl setDataRefUrl(String url);
	
	@Override
	default <T> T accept2(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefResourceFromUrl create(String url) {
		DataRefResourceFromUrl result = ModelFactory.createDefaultModel().createResource().as(DataRefResourceFromUrl.class)
				.setDataRefUrl(url);
		return result;
	}
}
