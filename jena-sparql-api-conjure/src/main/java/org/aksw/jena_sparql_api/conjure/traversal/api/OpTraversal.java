package org.aksw.jena_sparql_api.conjure.traversal.api;

import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

/**
 * A binding operation is a function that for a given 
 * rdf term in an rdf graph yields another set of rdf terms.
 * 
 * 
 * (RDFTerm, RDFGraph) -&gt; RDFTerm
 * 
 * 
 * @author raven
 *
 */
public interface OpTraversal
	extends Resource
{
	//@Override
	Collection<OpTraversal> getChildren();
	
	<T> T accept(OpTraversalVisitor<T> visitor);
}
