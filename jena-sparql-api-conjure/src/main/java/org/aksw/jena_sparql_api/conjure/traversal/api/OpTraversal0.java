package org.aksw.jena_sparql_api.conjure.traversal.api;

import java.util.Collection;
import java.util.Collections;

public interface OpTraversal0
	extends OpTraversal
{
	@Override
	default Collection<OpTraversal> getChildren() {
		return Collections.emptyList();
	}
}
