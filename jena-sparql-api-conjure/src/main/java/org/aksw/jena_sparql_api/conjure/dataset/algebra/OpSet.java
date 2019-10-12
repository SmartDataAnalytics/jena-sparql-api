package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Set a variable in the execution context
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpSet
	extends Op1
{
	@Iri("rpif:queryString")
	Set<String> getQueryStrings();
	OpSet setQueryStrings(Collection<String> queryStrings);
	
	@Override
	OpSet setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpSet create(Model model, Op subOp, String queryString) {
		OpSet result = create(model, subOp, Collections.singleton(queryString));
		
		return result;
	}
	
	public static OpSet create(Model model, Op subOp, Collection<String> queryStrings) {
		OpSet result = model.createResource().as(OpSet.class)
			.setSubOp(subOp)
			.setQueryStrings(queryStrings);
		
		return result;
	}
}