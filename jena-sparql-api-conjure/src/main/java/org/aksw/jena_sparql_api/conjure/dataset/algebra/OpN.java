package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface OpN
	extends Op
{
	@Iri("eg:arg")
	@PolymorphicOnly
	List<Op> getSubOps();
	
	OpN setSubOps(List<Op> subOps);
}
