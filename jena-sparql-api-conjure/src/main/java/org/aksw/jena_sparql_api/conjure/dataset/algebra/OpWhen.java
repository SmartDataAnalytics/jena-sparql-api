package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpWhen
	extends Op1
{
	@Iri("rpif")
	String getCondition();
	OpWhen setCondition(String condition);
	
	@Override
	OpWhen setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpWhen create(Model model, Op subOp, String condition) {
		OpWhen result = model.createResource().as(OpWhen.class)
			.setSubOp(subOp)
			.setCondition(condition);
		
		return result;
	}
}
