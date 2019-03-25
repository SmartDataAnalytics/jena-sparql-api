package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public interface RDFConnectionEx
	extends RDFConnection
{
	RDFConnectionMetaData getMetaData();
}
