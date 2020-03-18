package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface ResourceSpecInline
	extends ResourceSpec
{
	@IriNs("rpif")
	@IriType
	ResourceSpecInline setValue(String value);
	String setValue();

	default <T> T accept(ResourceSpecVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

	// TODO We could (should) add content type and encoding fields here
	// This then overlaps with RdfEntityInfo and should possibly be consolidated

	public static ResourceSpecInline create(Model model, String value) {
		ResourceSpecInline result = model.createResource().as(ResourceSpecInline.class)
				.setValue(value);
		return result;
	}
}
