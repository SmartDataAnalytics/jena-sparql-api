package org.aksw.jena_sparql_api.conjure.dataref.api;

import org.apache.jena.sparql.core.DatasetDescription;

public interface DataRefFromRemoteSparqlDataset
	extends DataRef
{
	String getUrl();
	DatasetDescription getDatsetDescription();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
