package org.aksw.jena_sparql_api.web.server.utils;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class TestSparqlServer {

	@Test
	public void testSparqlServer() throws Exception {
		int port = 7529;
		Server server = new FactoryBeanSparqlServer()
				.setSparqlServiceFactory(FluentQueryExecutionFactory.from(ModelFactory.createDefaultModel()).create())
				.setPort(port)
				.create();

		while (server.isStarting()) {
			Thread.sleep(1000);
		}

		RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/sparql");
		Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");

		server.stop();
		server.join();

	}
}
