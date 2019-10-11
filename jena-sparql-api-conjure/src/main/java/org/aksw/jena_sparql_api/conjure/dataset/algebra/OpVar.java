package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfTypeNs("rpif")
public interface OpVar
	extends Op0
{
	@IriNs("rpif")
	String getName();
	OpVar setName(String name);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpVar create(String name) {
		OpVar result = from(ModelFactory.createDefaultModel(), name);

		return result;
	}

	public static OpVar from(Model model, String name) {
		OpVar result = model
				.createResource().as(OpVar.class)
				.setName(name);

		return result;
	}
}
