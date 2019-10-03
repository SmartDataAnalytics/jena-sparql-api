package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collection;
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
	
	@Override
	default Collection<Op> getChildren() {
		List<Op> result = getSubOps();
		return result;
	}
}
