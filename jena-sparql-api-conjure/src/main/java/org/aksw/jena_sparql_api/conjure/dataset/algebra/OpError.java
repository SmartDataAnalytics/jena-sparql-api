package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Fail an execution
 * To be used in conjunction with OpWhen
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpError
	extends Op0
{
	@IriNs("rpif")
	String getReason();
	OpError setReason(String name);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpError clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpError.class);
	}

	
//	public static OpVar create(String name) {
//		OpVar result = create(ModelFactory.createDefaultModel(), name);
//
//		return result;
//	}

	public static OpError create(Model model, String name) {
		OpError result = model
				.createResource().as(OpError.class)
				.setReason(name);

		return result;
	}
}
