package org.aksw.jena_sparql_api.conjure.traversal.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface OpTraversal1
	extends OpTraversal
{
	@IriNs("rpif")
	@PolymorphicOnly
	OpTraversal getSubOp();
	OpTraversal1 setSubOp(OpTraversal op);
	
	@Override
	default Collection<OpTraversal> getChildren() {
		OpTraversal subOp = Objects.requireNonNull(getSubOp());
		return Collections.singletonList(subOp);
	}
}
