package org.aksw.jena_sparql_api.conjure.dataobject.api;

import org.apache.jena.rdfconnection.RDFConnection;

public interface DataObjectRdf
	extends DataObject
{
	RDFConnection getConnection();
}
