package org.aksw.jena_sparql_api.conjure.resourcespec;

import org.apache.jena.rdf.model.Resource;

public interface ResourceSpec
	extends Resource
{
	<T> T accept(ResourceSpecVisitor<T> visitor);
}
