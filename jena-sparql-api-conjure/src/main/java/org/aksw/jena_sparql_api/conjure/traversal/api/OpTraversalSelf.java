package org.aksw.jena_sparql_api.conjure.traversal.api;

import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Traversal from a node to itself
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpTraversalSelf
	extends OpTraversal0
{
	
	@Override
	default <T> T accept(OpTraversalVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpTraversalSelf create() {
		OpTraversalSelf result = create(ModelFactory.createDefaultModel());
		return result;
	}

	public static OpTraversalSelf create(Model model) {
		OpTraversalSelf result = model.createResource().as(OpTraversalSelf.class);

		return result;
	}
}
