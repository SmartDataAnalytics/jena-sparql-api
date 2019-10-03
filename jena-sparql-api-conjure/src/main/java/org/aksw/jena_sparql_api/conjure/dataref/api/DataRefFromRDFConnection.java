package org.aksw.jena_sparql_api.conjure.dataref.api;

import org.apache.jena.rdfconnection.RDFConnection;

public interface DataRefFromRDFConnection
	extends DataRef
{
	RDFConnection getConnection();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
