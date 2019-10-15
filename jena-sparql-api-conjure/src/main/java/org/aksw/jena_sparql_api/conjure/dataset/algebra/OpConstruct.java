package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpConstruct
	extends Op1
{
	@Iri("rpif:queryString")
	Set<String> getQueryStrings();
	OpConstruct setQueryStrings(Collection<String> queryStrings);
	
	@Override
	OpConstruct setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpConstruct clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpConstruct.class)
				.setSubOp(subOps.iterator().next())
				.setQueryStrings(getQueryStrings());
	}

	
	public static OpConstruct create(Model model, Op subOp, String queryString) {
		OpConstruct result = create(model, subOp, Collections.singleton(queryString));
		
		return result;
	}
	
	public static OpConstruct create(Model model, Op subOp, Collection<String> queryStrings) {
		OpConstruct result = model.createResource().as(OpConstruct.class)
			.setSubOp(subOp)
			.setQueryStrings(queryStrings);
		
		return result;
	}
}
