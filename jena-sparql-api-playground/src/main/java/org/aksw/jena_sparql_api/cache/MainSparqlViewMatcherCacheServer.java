package org.aksw.jena_sparql_api.cache;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewMatcherMaster;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionViewMatcherMaster;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionDecoratorBase;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.eclipse.jetty.server.Server;

import com.google.common.cache.CacheBuilder;

public class MainSparqlViewMatcherCacheServer {

	public static void main(String[] args) {
		JenaExtensionViewMatcher.register();


		mainTestQuery(args);
	}

	public static void mainTestQuery(String[] args) {

		QueryExecutionFactory qef = createQef();
		System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT * { ?s a <http://dbpedia.org/ontology/ResearchProject> }").execSelect()));

		QueryExecution qe = qef.createQueryExecution("SELECT * { ?s a <http://dbpedia.org/ontology/ResearchProject> }");
		System.out.println(ResultSetFormatter.asText(qe.execSelect()));
		QueryExecutionViewMatcherMaster x = QueryExecutionDecoratorBase.unwrap(QueryExecutionViewMatcherMaster.class, qe);
		System.out.println(x.getCacheHitLevel());

		qef.close();
	}

	public static QueryExecutionFactory createQef() {
		QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org")
				.config()
					.withDefaultLimit(1000, true)
				.end()
				.create();

		CacheBuilder<Object, Object> queryCacheBuilder = CacheBuilder.newBuilder().maximumSize(10000);

		ExecutorService executorService = Executors.newCachedThreadPool();

		QueryExecutionFactoryViewMatcherMaster tmp = QueryExecutionFactoryViewMatcherMaster.create(qef,
				queryCacheBuilder, executorService, true);

		qef = FluentQueryExecutionFactory.from(tmp)
				.config().withParser(SparqlQueryParserImpl.create()).end()
				.create();

		return qef;
	}

	public static void mainServer(String[] args) throws InterruptedException, IOException, URISyntaxException {

		/*
		 * Query query =
		 * QueryFactory.create("SELECT ?s { ?s ?p ?o } LIMIT 100000"); Op op =
		 * Algebra.compile(query); System.out.println("Before: " + op);
		 * Transform t = new TransformTopN(); op = Transformer.transform(t, op);
		 * System.out.println("After: " + op); Query q = OpAsQuery.asQuery(op);
		 * System.out.println("Result: " + q);
		 *
		 * if(true) { return; }
		 */

		// Create an implemetation of the view matcher - i.e. an object that
		// supports
		// - registering (Op, value) entries
		// - rewriting an Op using references to the registered ops

		QueryExecutionFactory qef = createQef();

		int port = 7531;
		Server server = FactoryBeanSparqlServer.newInstance()
				.setSparqlServiceFactory(qef)
				.setPort(port)
				.create();

		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:" + port + "/sparql"));
		}

		server.join();
	}
}
