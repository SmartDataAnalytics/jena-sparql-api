package org.aksw.jena_sparql_api.conjure.traversal.api;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Binding based on a SPARQL property path
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpPropertyPath
	extends OpTraversal0
{
//	@Override
//	OpPropertyPath setSubOp(OpTraversal subOp);

	@IriNs("rpif")
	String getPropertyPath();
	OpPropertyPath setPropertyPath(String propertyPath);

	@Override
	default <T> T accept(OpTraversalVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpPropertyPath create(Model model, String propertyPath) {
		OpPropertyPath result = model.createResource().as(OpPropertyPath.class)
			.setPropertyPath(propertyPath);
		
		return result;
	}
}
