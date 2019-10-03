package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpConstruct
	extends Op1
{
	@Iri("eg:queryString")
	Set<String> getQueryStrings();
	OpConstruct setQueryStrings(Collection<String> queryStrings);
	
	@Override
	OpConstruct setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpConstruct create(Op subOp, String queryString) {
		OpConstruct result = create(subOp, Collections.singleton(queryString));
		
		return result;
	}
	
	public static OpConstruct create(Op subOp, Collection<String> queryStrings) {
		OpConstruct result = subOp.getModel().createResource().as(OpConstruct.class)
			.setSubOp(subOp)
			.setQueryStrings(queryStrings);
		
		return result;
	}
}
