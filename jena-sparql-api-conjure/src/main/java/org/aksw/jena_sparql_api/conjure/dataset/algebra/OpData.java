package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * TODO Add attribute to hold data - right now its more of an OpEmpty
 * 
 * @author raven
 *
 */
@ResourceView
@RdfType
public interface OpData
	extends Op0
{
	
	// TODO Attribute for static data; may be empty
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpData create() {
		OpData result = create(ModelFactory.createDefaultModel());
		return result;
	}

	
	public static OpData create(Model model) {
		OpData result = model.createResource().as(OpData.class);

		return result;
	}

}