package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Op;

@ResourceView
@RdfType
public interface OpUnion
	extends Resource
{
	OpUnion setSubOps(List<Op> subOps);
	
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
