package org.aksw.jena_sparql_api.conjure.dataset.algebra;

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
