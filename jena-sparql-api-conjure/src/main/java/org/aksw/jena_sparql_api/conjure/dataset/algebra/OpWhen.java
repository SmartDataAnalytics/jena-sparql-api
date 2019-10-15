package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpWhen
	extends Op2
{
	@Iri("rpif")
	String getCondition();
	OpWhen setCondition(String condition);
	
	@Override
	OpWhen setLhs(Op subOp);

	@Override
	OpWhen setRhs(Op subOp);

	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpWhen clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpWhen.class)
				.setLhs(getLhs())
				.setRhs(getRhs())
				.setCondition(getCondition());
	}

	
	public static OpWhen create(Model model, String condition, Op lhsOp, Op rhsOp) {		
		OpWhen result = model.createResource().as(OpWhen.class)
			.setLhs(lhsOp)
			.setRhs(rhsOp)
			.setCondition(condition);
		
		return result;
	}
}
