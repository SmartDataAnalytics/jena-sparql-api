package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfTypeNs("rpif")
public interface OpUnion
	extends OpN
{
	@Override
	OpUnion setSubOps(List<Op> subOps);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

	@Override
	default OpUnion clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpUnion.class)
				.setSubOps(subOps);
	}

	
	public static OpUnion create(Model model, Op ...subOps) {
		return create(model, Arrays.asList(subOps));
	}

	public static OpUnion create(Model model, List<Op> subOps) {
//		Model model = subOps.size() > 0 ? subOps.get(0).getModel() : ModelFactory.createDefaultModel();
		// Model model = ModelFactory.createDefaultModel();
		model = model != null ? model : ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			model.add(subOp.getModel());
		}
		
		OpUnion result = model.createResource().as(OpUnion.class)
			.setSubOps(subOps);
		
		return result;
	}
}
