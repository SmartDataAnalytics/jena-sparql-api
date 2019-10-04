package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;

public interface DataRefOp
	extends DataRef
{
	Op getOp();

	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}

}
