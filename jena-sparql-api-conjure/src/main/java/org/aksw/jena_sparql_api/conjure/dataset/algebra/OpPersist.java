package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpPersist
	extends Op1
{
	OpPersist setSubOp(Op subOp);
	
	
	// TODO Add persistence / cache control attributes
	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpPersist clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpPersist.class)
				.setSubOp(subOps.iterator().next());
	}

//	public static OpPersist create(Op subOp) {
//		OpPersist result = create(subOp, Collections.singleton(queryString));
//		
//		return result;
//	}
	
	public static OpPersist create(Model model, Op subOp) {
		OpPersist result = model.createResource().as(OpPersist.class)
			.setSubOp(subOp);
		
		return result;
	}
}
