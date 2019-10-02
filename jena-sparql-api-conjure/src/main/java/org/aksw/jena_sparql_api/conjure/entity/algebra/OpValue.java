package org.aksw.jena_sparql_api.conjure.entity.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfType("eg:OpValue")
public interface OpValue
	extends Op0
{
	@IriNs("eg")
	Object getValue();
	OpValue setValue(Object value);

	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpValue create(Model model, Object value) {
		OpValue result = model.createResource().as(OpValue.class)
			.setValue(value);

		return result;
	}
}