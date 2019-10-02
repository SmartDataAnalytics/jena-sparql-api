package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.util.Collections;
import java.util.List;

public interface Op0
	extends Op
{
	@Override
	default List<Op> getSubOps() {
		return Collections.emptyList();
	}
}