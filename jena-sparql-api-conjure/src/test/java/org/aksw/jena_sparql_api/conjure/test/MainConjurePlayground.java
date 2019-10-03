package org.aksw.jena_sparql_api.conjure.test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataobject.impl.DataObjects;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefResourceFromUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainConjurePlayground {
	private static final Logger logger = LoggerFactory.getLogger(MainConjurePlayground.class);
	
	public static void main(String[] args) throws Exception {
		
		// Set up a conjure dataset processing template
		// Lets count predicates
		Op a = OpVar.create("dataRef");
		Op conjureWorkflow = OpConstruct.create(a, 
				"CONSTRUCT { ?p <eg:numUses> ?c } WHERE { { SELECT ?p (COUNT(*) AS ?c) { ?s ?p ?o } GROUP BY ?p } }");

		// Set up a simple conjure workflow executor
		HttpResourceRepositoryFromFileSystem repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();		
		OpExecutorDefault executor = new OpExecutorDefault(repo);

		
		// So far so good - all we need now, is some data and we can start execution

		
		// Create a SPARQL parser with preconfigured prefixes
		// Pure luxury!
		Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false);
		
		// Fetch some download urls from the databus or limbo
		// Turns out both data catalogs have quality issues ;)
		List<String> urls;
//		try(RdfDataObject catalog = DataObjects.fromSparqlEndpoint("https://databus.dbpedia.org/repo/sparql", null, null)) {			
		try(RdfDataObject catalog = DataObjects.fromUrl("https://gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl")) {			
			try(RDFConnection conn = catalog.openConnection()) {
				urls = SparqlRx.execSelect(conn,
//						"SELECT DISTINCT ?o { ?s <http://www.w3.org/ns/dcat#downloadURL> ?o } LIMIT 10")
						parser.apply("SELECT DISTINCT ?o { ?s dataid:group ?g ; dcat:distribution/dcat:downloadURL ?o ; } LIMIT 10")
							.getAsQueryStmt().getQuery())
					.map(qs -> qs.get("o"))
					.map(RDFNode::toString)
					.toList()
					.blockingGet();				
			}			
		}

		
		// Ready for workflow execution!

		logger.info("Retrieved " + urls.size() + " urls for processing " + urls);
		
		for(String url : urls) {
			logger.info("Processing: " + url);

			// Create a copy of the workflow spec and substitute the variables
			Map<String, Op> map = Collections.singletonMap("dataRef", OpDataRefResource.from(DataRefResourceFromUrl.create(url)));			
			Op effectiveWorkflow = OpUtils.copyWithSubstitution(conjureWorkflow, map::get);			
			

			// Set up a dataset processing expression		
			logger.info("Conjure spec is:");
			RDFDataMgr.write(System.err, effectiveWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);
			
			try(RdfDataObject data = effectiveWorkflow.accept(executor)) {
				try(RDFConnection conn = data.openConnection()) {
					Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					
					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
				}
			} catch(Exception e) {
				logger.warn("Failed to process " + url, e);
			}
		}

	}
}
