package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@RdfTypeNs("rpif")
public interface ResourceSpecUrl
	extends Resource
{
	@IriNs("rpif")
	@IriType
	ResourceSpecUrl setResourceUrl(String url);
	String getResourceUrl();

	default <T> T accept(ResourceSpecVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

	public static ResourceSpecUrl create(Model model, String url) {
		ResourceSpecUrl result = model.createResource().as(ResourceSpecUrl.class)
				.setResourceUrl(url);
		return result;
	}
}
