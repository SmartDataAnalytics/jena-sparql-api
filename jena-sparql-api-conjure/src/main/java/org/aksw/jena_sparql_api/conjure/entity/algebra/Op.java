package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.util.List;

import org.apache.jena.rdf.model.Resource;

public interface Op
	extends Resource
{
	List<Op> getSubOps();
	
	<T> T accept(OpVisitor<T> visitor);
}