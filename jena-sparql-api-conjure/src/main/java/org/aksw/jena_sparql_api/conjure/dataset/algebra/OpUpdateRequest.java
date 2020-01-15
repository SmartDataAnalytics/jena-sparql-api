package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpUpdateRequest
	extends Op1
{
//	@IriNs("eg")
//	@PolymorphicOnly
//	Op getSubOp();
//	OpUpdateRequest setSubOp(Op op);
	
	@Override
	OpUpdateRequest setSubOp(Op op);		

	
	@IriNs("rpif")
	List<String> getUpdateRequests();
	OpUpdateRequest setUpdateRequests(Collection<String> updateRequestStrings);
	
	default OpUpdateRequest addUpdateRequest(String updateRequestString) {
		Collection<String> tmp = getUpdateRequests();
		tmp.add(updateRequestString);
		
		return this;
	}
	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpUpdateRequest clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpUpdateRequest.class)
				.setSubOp(subOps.iterator().next())
				.setUpdateRequests(getUpdateRequests());
	}

	
	public static OpUpdateRequest create(Model model, Op subOp, String updateRequestStrings) {
		OpUpdateRequest result = create(model, subOp, Collections.singleton(updateRequestStrings));
		
		return result;
	}
	
	public static OpUpdateRequest create(Model model, Op subOp, Collection<String> updateRequestStrings) {
		OpUpdateRequest result = model.createResource().as(OpUpdateRequest.class)
			.setSubOp(subOp)
			.setUpdateRequests(updateRequestStrings);
		
		return result;
	}


}
