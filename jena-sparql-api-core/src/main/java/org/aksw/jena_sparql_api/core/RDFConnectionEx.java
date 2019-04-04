package org.aksw.jena_sparql_api.core;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionEx
	extends RDFConnection
{
	RDFConnectionMetaData getMetaData();
}
