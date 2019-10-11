package org.aksw.jena_sparql_api.conjure.traversal.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface OpTraversal2
	extends OpTraversal
{
	@IriNs("rpif")
	@PolymorphicOnly
	OpTraversal getLhs();
	OpTraversal2 setLhs(OpTraversal lhs);
		
	@IriNs("rpif")
	@PolymorphicOnly
	OpTraversal getRhs();
	OpTraversal2 setRhs(OpTraversal rhs);

	@Override
	default Collection<OpTraversal> getChildren() {
		OpTraversal lhs = Objects.requireNonNull(getLhs());
		OpTraversal rhs = Objects.requireNonNull(getRhs());
		return Arrays.asList(lhs, rhs);
	}
}
