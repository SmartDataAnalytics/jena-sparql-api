package org.aksw.jena_sparql_api.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewMatcherMaster;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.eclipse.jetty.server.Server;

import com.google.common.cache.CacheBuilder;

public class MainSparqlViewMatcherCacheServer {
	public static void main(String[] args) throws InterruptedException {
		JenaExtensionViewMatcher.register();

		/*
		Query query = QueryFactory.create("SELECT ?s { ?s ?p ?o } LIMIT 100000");
		Op op = Algebra.compile(query);
		System.out.println("Before: " + op);
		Transform t = new TransformTopN();
		op = Transformer.transform(t,  op);
		System.out.println("After: " + op);
		Query q = OpAsQuery.asQuery(op);
		System.out.println("Result: " + q);

		if(true) {
			return;
		}
		*/

		// Create an implemetation of the view matcher - i.e. an object that supports
		// - registering (Op, value) entries
		// - rewriting an Op using references to the registered ops
		CacheBuilder<Object, Object> queryCacheBuilder = CacheBuilder.newBuilder()
				.maximumSize(10000);

        QueryExecutionFactory qef = FluentQueryExecutionFactory
        		.http("http://dbpedia.org/sparql", "http://dbpedia.org")
        		.config()
        			.withDefaultLimit(1000, true)
        		.end()
        		.create();
        ExecutorService executorService = Executors.newCachedThreadPool();

        QueryExecutionFactoryViewMatcherMaster tmp = QueryExecutionFactoryViewMatcherMaster.create(qef, queryCacheBuilder, executorService);

        Server server = FactoryBeanSparqlServer.newInstance()
        	.setSparqlServiceFactory(tmp)
        	.create();

        server.join();
	}
}
