package org.aksw.jena_sparql_api.cache.tests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.OpRewriteViewMatcherStateful;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewMatcherMaster;
import org.aksw.jena_sparql_api.concept_cache.core.StorageEntry;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.parse.QueryExecutionFactoryParse;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SparqlViewMatcherCacheTests {

	private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherCacheTests.class);

	@Test
	public void test() throws Exception {
		JenaExtensionViewMatcher.register();

		Resource r = new ClassPathResource("data-lorenz.nt");
		Model model = ModelFactory.createDefaultModel();
		model.read(r.getInputStream(), "http://ex.org/", "NTRIPLES");

		// Create an implemetation of the view matcher - i.e. an object that supports
		// - registering (Op, value) entries
		// - rewriting an Op using references to the registered ops
		RemovalListenerMultiplexer<Node, StorageEntry> removalListeners = new RemovalListenerMultiplexer<>();

		Cache<Node, StorageEntry> queryCache = CacheBuilder.newBuilder()
				.maximumSize(10000)
				.removalListener(removalListeners)
				.build();

		OpRewriteViewMatcherStateful viewMatcherRewriter = new OpRewriteViewMatcherStateful(queryCache, removalListeners.getClients());

		// Obtain the global service map for registering temporary handlers for <view://...> SERVICEs
		// for the duration of a query execution
		// Note: JenaExtensionViewMatcher.register(); already registered this object at ARQ's global query execution context

		// A map which associates SERVICE ids with an interface for fetching slices of data.
		// Map<Node, RangedSupplier<Long, Binding>> dataSupplier;

        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model).create();
        ExecutorService executorService = Executors.newCachedThreadPool();

        qef = new QueryExecutionFactoryViewMatcherMaster(qef, viewMatcherRewriter, executorService);
        qef = new QueryExecutionFactoryParse(qef, SparqlQueryParserImpl.create());

        Stopwatch sw = Stopwatch.createStarted();

        for(int i = 0; i < 3; ++i) {
        	{
    	        System.out.println("Cache size before: " + queryCache.size());

    	        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> } Limit 10");
		        ResultSet rs = qe.execSelect();
    	        System.out.println(ResultSetFormatter.asText(rs));

    	        System.out.println("Cache size after: " + queryCache.size());
//	        	ResultSetFormatter.consume(rs);
        	}
//        	{
//    	        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> ; a <foo://bar> } Limit 10");
//    	        ResultSet rs = qe.execSelect();
//    	        System.out.println(ResultSetFormatter.asText(rs));
//    	        //System.out.println(t);
//            	//ResultSetFormatter.consume(rs);
//        	}
        }

        logger.info("Awaiting termination of thread pool...");
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        logger.info("done.");
	}
}
