package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.util.Collection;
import java.util.Collections;

public interface Op0
	extends Op
{
	@Override
	default Collection<Op> getChildren() {
		return Collections.emptyList();
	}
}