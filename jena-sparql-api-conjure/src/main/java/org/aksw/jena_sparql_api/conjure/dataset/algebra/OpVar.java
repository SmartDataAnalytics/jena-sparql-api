package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

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
	
	@Override
	default OpVar clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpVar.class)
				.setName(getName());
	}


//	public static OpVar create(String name) {
//		OpVar result = create(ModelFactory.createDefaultModel(), name);
//
//		return result;
//	}

	public static OpVar create(Model model, String name) {
		OpVar result = model
				.createResource().as(OpVar.class)
				.setName(name);

		return result;
	}
}
