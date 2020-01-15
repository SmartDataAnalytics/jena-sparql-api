package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.util.Collection;

import org.apache.jena.rdf.model.Resource;

public interface Op
	extends Resource //, TreeLike<Op>
{	
	//@Override
	Collection<Op> getChildren();

	<T> T accept(OpVisitor<T> visitor);
}