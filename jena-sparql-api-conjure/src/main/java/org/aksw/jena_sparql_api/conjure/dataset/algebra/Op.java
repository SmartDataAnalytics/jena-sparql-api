package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.apache.jena.rdf.model.Resource;

public interface Op
	extends Resource
{
	<T> T accept(OpVisitor<T> visitor);
}
