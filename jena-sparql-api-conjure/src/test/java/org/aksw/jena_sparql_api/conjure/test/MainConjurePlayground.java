package org.aksw.jena_sparql_api.conjure.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.model.RDFNodeJsonUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainConjurePlayground {
	private static final Logger logger = LoggerFactory.getLogger(MainConjurePlayground.class);
	
	public static void main(String[] args) throws Exception {
		
		// TODO Circular init issue with DefaultPrefixes
		// We could use ARQConstants.getGlobalPrefixMap()
		JenaSystem.init();
		
		// Create a SPARQL parser with preconfigured prefixes
		// Pure luxury!
		Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false);

		
		// Set up a conjure dataset processing template
		// Lets count predicates - 'dataRef' is a placeholder for datasets to work upon
		Op v = OpVar.create("dataRef");
		Op countPredicates = OpConstruct.create(v, 
				parser.apply("CONSTRUCT { ?p <eg:numUses> ?c } WHERE { { SELECT ?p (COUNT(*) AS ?c) { ?s ?p ?o } GROUP BY ?p } }").toString());

		// Let's run a CONSTRUCT query on the output of another CONSTRUCT query
		// (because we can)
		Op totalCount = OpConstruct.create(countPredicates, parser.apply("CONSTRUCT { <urn:report> <urn:totalUses> ?t } WHERE { { SELECT (SUM(?c) AS ?t) { ?s <eg:numUses> ?c } } }").toString());

		Op reportDate = OpUpdateRequest.create(totalCount,
				parser.apply("INSERT { <urn:report> <urn:generationDate> ?d } WHERE { BIND(NOW() AS ?d) }").toString());

		Op anonymousConjureWorkflow = OpUnion.create(countPredicates, reportDate);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String str = RDFNodeJsonUtils.toJsonNodeString(anonymousConjureWorkflow, gson);
		System.out.println(str);
		RDFNode tmp = RDFNodeJsonUtils.toRDFNode(str, gson);
		
		anonymousConjureWorkflow = JenaPluginUtils.polymorphicCast(tmp, Op.class);
		
		/* Example RDF output:

			<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
        		<eg:numUses>  1 .

			<http://www.w3.org/2000/01/rdf-schema#label>
        		<eg:numUses>  1 .

			<urn:report>  <urn:generationDate>  "2019-10-04T02:26:34.588+02:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
        		<urn:totalUses>       2 .	
		 */
		
		
		// Hmm.. let's give the workflow a name and save it for use in the future
		String workflowUri = "urn:myWorkflow";
		Resource workflowRes = ResourceUtils.renameResource(anonymousConjureWorkflow, workflowUri);
		
		Path tmpFile = Files.createTempFile("workflow-", ".ttl");
		RDFDataMgr.write(Files.newOutputStream(tmpFile), workflowRes.getModel(), RDFFormat.TURTLE_PRETTY);
		
		// off to sleep
		// ZZZZzzzzzz.....____
		// ok. enough sleep, the future is now!

		Model deserializedWorkflowModel = RDFDataMgr.loadModel(tmpFile.toString());		
		Files.delete(tmpFile);
		Resource deserializedWorkflowRes = deserializedWorkflowModel.createResource(workflowUri);
		
		// Cast the Resource back to the appropriate Op sub class
		// (The mapper-proxy plugin system knows how to do that)
		Op conjureWorkflow = JenaPluginUtils.polymorphicCast(deserializedWorkflowRes, Op.class);
		
		// Print out the deserialized workflow for inspection
		RDFDataMgr.write(System.err, conjureWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);

		
		// In case you missed it because you couldn't see it: The workflow *IS* RDF:
		// We created RDF using static factory methods, saved it to disk and loaded it again		
		

		// Set up a simple file-system based conjure repository and workflow executor
		// This does HTTP caching, content type conversion and content negotiation
		// Lots of magic, fairies and unicorns in there
		// (and gears and screws one wants to configure for production use - and which may at this stage sometimes break)
		HttpResourceRepositoryFromFileSystem repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();		
		OpExecutorDefault executor = new OpExecutorDefault(repo);

		// Get a copy of the limbo dataset catalog via the repo so that it gets cached
		DataRef dataRef1 = DataRefUrl.create("https://gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl");
		
		// Or set up a workflow that makes databus available
		DataRef dataRef2 = DataRefSparqlEndpoint.create("https://databus.dbpedia.org/repo/sparql");

		
		// Create a data ref from a workflow
		DataRef dataRef3 = DataRefOp.create(
				OpUpdateRequest.create(OpData.create(),
//					parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
						parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <http://localhost/~raven/bib_lds_20190305.hdt.gz> ] }").toString()));

		
		DataRef dataRef = dataRef3;
		
		// Set up the workflow that makes a digital copy of a dataset available
		Op basicWorkflow = OpDataRefResource.from(dataRef);
		
		
		// So far so good - all we need now, is some data and we can start execution

		
		// Fetch some download urls from the databus or limbo
		// Turns out both data catalogs have quality issues ;)
		List<String> urls;
//		try(RdfDataObject catalog = DataObjects.fromSparqlEndpoint("https://databus.dbpedia.org/repo/sparql", null, null)) {			
		try(RdfDataPod catalog = basicWorkflow.accept(executor)) {			
			try(RDFConnection conn = catalog.openConnection()) {
				urls = SparqlRx.execSelect(conn,
//						"SELECT DISTINCT ?o { ?s <http://www.w3.org/ns/dcat#downloadURL> ?o } LIMIT 10")
						parser.apply("SELECT DISTINCT ?o { ?s dataid:group ?g ; dcat:distribution/dcat:downloadURL ?o } LIMIT 10")
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
			Map<String, Op> map = Collections.singletonMap("dataRef", OpDataRefResource.from(DataRefUrl.create(url)));			
			Op effectiveWorkflow = OpUtils.copyWithSubstitution(conjureWorkflow, map::get);			
			

			// Set up a dataset processing expression		
			logger.info("Conjure spec is:");
			RDFDataMgr.write(System.err, effectiveWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);
			
			try(RdfDataPod data = effectiveWorkflow.accept(executor)) {
				try(RDFConnection conn = data.openConnection()) {
					// Print out the data that is the process result
					Model model = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					
					RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
				}
			} catch(Exception e) {
				logger.warn("Failed to process " + url, e);
			}
		}

	}
}
