package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpHdtHeader
	extends Op1
{
	
	@Override
	OpHdtHeader setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpHdtHeader clone(Model cloneModel, List<Op> subOp) {
		return this.inModel(cloneModel).as(OpHdtHeader.class)
				.setSubOp(subOp.iterator().next());
	}
	
//	public static OpHdtHeader create(Op subOp) {
//		OpHdtHeader result = create(subOp);
//		
//		return result;
//	}
	
	public static OpHdtHeader create(Model model, Op subOp) {
		OpHdtHeader result = model.createResource().as(OpHdtHeader.class)
			.setSubOp(subOp);

		return result;
	}
}
