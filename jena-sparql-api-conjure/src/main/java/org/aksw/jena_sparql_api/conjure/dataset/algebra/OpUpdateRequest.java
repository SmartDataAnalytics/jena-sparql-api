package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpUpdateRequest
	extends Op1
{
//	@IriNs("eg")
//	@PolymorphicOnly
//	Op getSubOp();
//	OpUpdateRequest setSubOp(Op op);
	
	@Override
	OpUpdateRequest setSubOp(Op op);		

	
	@IriNs("eg")
	List<String> getUpdateRequests();
	OpUpdateRequest setUpdateRequests(List<String> updateRequestStrings);
	
	default OpUpdateRequest addUpdateRequest(String updateRequestString) {
		Collection<String> tmp = getUpdateRequests();
		tmp.add(updateRequestString);
		
		return this;
	}
	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

}
