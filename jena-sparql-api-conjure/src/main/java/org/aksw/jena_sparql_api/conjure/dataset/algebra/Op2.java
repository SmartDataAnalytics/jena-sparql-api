package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface Op2
	extends Op
{
	@IriNs("rpif")
	@PolymorphicOnly
	Op getLhs();
	Op2 setLhs(Op op);
	
	@IriNs("rpif")
	@PolymorphicOnly
	Op getRhs();
	Op2 setRhs(Op op);

	@Override
	default List<Op> getChildren() {
		Op lhs = Objects.requireNonNull(getLhs());
		Op rhs = Objects.requireNonNull(getRhs());

		return Arrays.asList(lhs, rhs);
	}
}
