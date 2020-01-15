package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfTypeNs("rpif")
public interface OpSequence
	extends OpN
{
	@Override
	OpSequence setSubOps(List<Op> subOps);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpSequence clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpSequence.class)
				.setSubOps(subOps);
	}

	
	public static OpSequence create(Model model, Op ...subOps) {
		return create(model, Arrays.asList(subOps));
	}

	public static OpSequence create(Model model, List<Op> subOps) {
		//Model model = subOps.size() > 0 ? subOps.get(0).getModel() : ModelFactory.createDefaultModel();
		model = model != null ? model : ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			model.add(subOp.getModel());
		}
		
		OpSequence result = model.createResource().as(OpSequence.class)
			.setSubOps(subOps);
		
		return result;
	}
}
