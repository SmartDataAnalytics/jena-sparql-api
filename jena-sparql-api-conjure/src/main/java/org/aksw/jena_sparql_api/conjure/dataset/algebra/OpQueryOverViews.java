package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Similar to an OpUnion, but without the materialization
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpQueryOverViews
	extends Op1
{
//	@Iri("rpif:views")
//	List<ResourceSpec> getViews();
//	OpQueryOverViews setViews(Collection<ResourceSpec> views);

	@Iri("rpif:views")
	List<String> getViewDefs();
	OpQueryOverViews setViewDefs(Collection<String> views);
	
	@Override
	OpQueryOverViews setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpQueryOverViews clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpQueryOverViews.class)
				.setSubOp(subOps.iterator().next())
				.setViewDefs(getViewDefs());
	}

	
	public static OpQueryOverViews create(Model model, Op subOp, String queryString) {
		OpQueryOverViews result = create(model, subOp, Collections.singleton(queryString));
		
		return result;
	}
	
	public static OpQueryOverViews create(Model model, Op subOp, Collection<String> views) {
		OpQueryOverViews result = model.createResource().as(OpQueryOverViews.class)
			.setSubOp(subOp)
			.setViewDefs(views);
		
		return result;
	}
}
