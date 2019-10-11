package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfTypeNs("rpif")
public interface OpCoalesce
	extends OpN
{
	@Override
	OpCoalesce setSubOps(List<Op> subOps);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	
	public static OpCoalesce create(Op ...subOps) {
		return create(Arrays.asList(subOps));
	}

	public static OpCoalesce create(List<Op> subOps) {
		//Model model = subOps.size() > 0 ? subOps.get(0).getModel() : ModelFactory.createDefaultModel();
		Model model = ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			model.add(subOp.getModel());
		}
		
		OpCoalesce result = model.createResource().as(OpCoalesce.class)
			.setSubOps(subOps);
		
		return result;
	}
}
