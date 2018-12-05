package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.aksw.jena_sparql_api.sparql.ext.io.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.junit.Test;

public class TestSparqlFs {
	@Test
	public void testSparqlFs() {
		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create())) {
			try(QueryExecution qe = conn.query("PREFIX nio: <" + JenaExtensionFs.ns + ">\n"
					+ "PREFIX url: <" + JenaExtensionUrl.ns + ">\n"
					+ "SELECT * { <> nio:find ?o . FILTER(REGEX(STR(?o), 'xml$', 'i')) ?o url:text ?c } LIMIT 1")) {
				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
		}
	}
}
