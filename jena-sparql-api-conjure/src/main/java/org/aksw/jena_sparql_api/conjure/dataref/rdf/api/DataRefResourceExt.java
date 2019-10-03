package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;

public interface DataRefResourceExt
	extends DataRefExt, DataRefResource
{
	@Override
	default <T> T accept2(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
