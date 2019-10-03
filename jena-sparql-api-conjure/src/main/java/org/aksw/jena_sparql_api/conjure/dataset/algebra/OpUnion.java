package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface OpUnion
	extends OpN
{
	@Override
	OpUnion setSubOps(List<Op> subOps);

	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	
	public static OpUnion create(Op ...subOps) {
		Model model = subOps.length > 0 ? subOps[0].getModel() : ModelFactory.createDefaultModel();
		
		OpUnion result = model.createResource().as(OpUnion.class)
			.setSubOps(Arrays.asList(subOps));
		
		return result;
	}
}
