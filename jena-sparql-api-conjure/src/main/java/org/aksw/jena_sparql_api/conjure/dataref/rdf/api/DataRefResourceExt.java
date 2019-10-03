package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefResource;

public interface DataRefResourceExt
	extends DataRefResource, DataRefExt
{
	default <T> T accept(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
