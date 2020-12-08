package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionEx
	extends RDFConnection
{
	RDFConnectionMetaData getMetaData();
}
