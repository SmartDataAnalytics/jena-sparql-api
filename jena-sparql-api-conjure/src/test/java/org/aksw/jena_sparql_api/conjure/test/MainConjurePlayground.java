package org.aksw.jena_sparql_api.conjure.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.dcat.ap.utils.DcatUtils;
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
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilder;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureContext;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureFluent;
import org.aksw.jena_sparql_api.conjure.fluent.QLib;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobBinding;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.conjure.traversal.engine.FunctionAssembler;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.io.json.RDFNodeJsonUtils;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainConjurePlayground {
	private static final Logger logger = LoggerFactory.getLogger(MainConjurePlayground.class);
	
	public static void main2(String[] args) {
		
		String url = "http://localhost/~raven/test.hdt";


		ConjureContext ctx = new ConjureContext();
		Model model = ctx.getModel();

		ConjureBuilder cj = new ConjureBuilderImpl(ctx);

		
		Op op = cj.coalesce(
				cj.fromUrl(url).hdtHeader().construct("CONSTRUCT WHERE { ?s <urn:tripleCount> ?o }"),
				cj.fromUrl(url).tripleCount().cache()).getOp();

		Job job = Job.create(model);
		job.setOp(op);
		job.getJobBindings().add(JobBinding.create(model, "datasetId", OpTraversalSelf.create(model)));

		

		// Goal: spark-submit-cj.sh catalog macrolib macroname	
//		cj.call("myMacro", cj.fromUrl(url));

		
		RDFDataMgr.write(System.out, job.getModel(), RDFFormat.TURTLE_PRETTY);
	}


	public static void main(String[] args) throws Exception {
		
		// TODO Circular init issue with DefaultPrefixes
		// We could use ARQConstants.getGlobalPrefixMap()
		JenaSystem.init();
		
		// Create a SPARQL parser with preconfigured prefixes
		// Pure luxury!
		Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false);

		Model model = ModelFactory.createDefaultModel();
		
		// Set up a conjure dataset processing template
		// Lets count predicates - 'dataRef' is a placeholder for datasets to work upon
		Op v = OpVar.create(model, "dataRef");
		Op countPredicates = OpConstruct.create(model, v, 
				parser.apply("CONSTRUCT { ?p <eg:numUses> ?c } WHERE { { SELECT ?p (COUNT(*) AS ?c) { ?s ?p ?o } GROUP BY ?p } }").toString());

		// Let's run a CONSTRUCT query on the output of another CONSTRUCT query
		// (because we can)
		Op totalCount = OpConstruct.create(model, countPredicates, parser.apply("CONSTRUCT { <urn:report> <urn:totalUses> ?t } WHERE { { SELECT (SUM(?c) AS ?t) { ?s <eg:numUses> ?c } } }").toString());

		Op reportDate = OpUpdateRequest.create(model, totalCount,
				parser.apply("INSERT { <urn:report> <urn:generationDate> ?d } WHERE { BIND(NOW() AS ?d) }").toString());

		Op anonymousConjureWorkflow = OpUnion.create(null, countPredicates, reportDate);
		
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
		
		
	    conjureWorkflow = OpConstruct.create(model, v, parser.apply(
	    	      "CONSTRUCT {\n" + 
	    	      "	    	          <env:datasetId> <urn:count> ?c\n" + 
	    	      "	    	        } {\n" + 
	    	      "	    	          { SELECT (COUNT(*) AS ?c) {\n" + 
	    	      "	    	            ?s ?p ?o\n" + 
	    	      "	    	          } }\n" + 
	    	      "	    	        }").toString());

	    
	    
		ConjureContext ctx = new ConjureContext();
		Model xmodel = ctx.getModel();
		xmodel.setNsPrefix("rpif", DefaultPrefixes.prefixes.getNsPrefixURI("rpif"));

		ConjureBuilder cj = new ConjureBuilderImpl(ctx);
	    
		// Example - Read triples from the header or fall back to actual counting
		if (false) {
			conjureWorkflow = cj.coalesce(
					cj.fromVar("dataRef").hdtHeader().everthing(),
					cj.fromVar("dataRef").construct(QLib.tripleCount()).cache()).getOp();
	    }

		// Example - Read dataset id from the header and pass it into the generated report

		
		ConjureFluent dataset = cj.fromVar("dataRef");
		conjureWorkflow =
				cj.union(
					dataset.hdtHeader()
						.construct("CONSTRUCT WHERE { ?s a <http://rdfs.org/ns/void#Dataset> }")
						//.set("datasetId", "SELECT ?s { ?s a <http://rdfs.org/ns/void#Dataset> }", null),
						.set("datasetId", "SELECT ?s { ?s ?p ?o }", null),
					dataset.construct("CONSTRUCT { ?b a <urn:Report> ; <urn:usesProperty> ?p } { BIND(BNODE() AS ?b) { SELECT DISTINCT ?p { ?s ?p ?o } } }")
				)
				.update("INSERT { ?s <urn:hasReport> ?b } WHERE { ?s a <http://rdfs.org/ns/void#Dataset> . ?b a <urn:Report> }")
					.getOp();
		
		Job job = Job.create(xmodel);
		job.setOp(conjureWorkflow);
		job.setJobBindings(Arrays.asList(JobBinding.create(xmodel, "datasetId", OpTraversalSelf.create(xmodel))));


		
		
		// Print out the deserialized workflow for inspection
		RDFDataMgr.write(System.err, job.getModel(), RDFFormat.TURTLE_PRETTY);

//		if(true) {
//			return;
//		}
		
		// In case you missed it because you couldn't see it: The workflow *IS* RDF:
		// We created RDF using static factory methods, saved it to disk and loaded it again		
		

		// Set up a simple file-system based conjure repository and workflow executor
		// This does HTTP caching, content type conversion and content negotiation
		// Lots of magic, fairies and unicorns in there
		// (and gears and screws one wants to configure for production use - and which may at this stage sometimes break)
		HttpResourceRepositoryFromFileSystem repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();		
		OpExecutorDefault executor = new OpExecutorDefault(repo);

		// Get a copy of the limbo dataset catalog via the repo so that it gets cached
		DataRef dataRef1 = DataRefUrl.create(model, "https://gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl");
		
		// Or set up a workflow that makes databus available
		DataRef dataRef2 = DataRefSparqlEndpoint.create("https://databus.dbpedia.org/repo/sparql");

		
		// Create a data ref from a workflow
		DataRef dataRef3 = DataRefOp.create(
				OpUpdateRequest.create(model, OpData.create(model),
//					parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
						parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <http://localhost/~raven/bib_lds_20190305.hdt.gz> ] }").toString()));

		DataRef dataRef4 = DataRefOp.create(
				OpUpdateRequest.create(model, OpData.create(model),
//					parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
						parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <https://data.dnb.de/opendata/zdb_lds.hdt.gz> ] }").toString()));


		DataRef dataRef = dataRef4;
		
		// Set up the workflow that makes a digital copy of a dataset available
		Op basicWorkflow = OpDataRefResource.from(model, dataRef);
		
		
		// So far so good - all we need now, is some data and we can start execution

		
		// Fetch some download urls from the databus or limbo
		// Turns out both data catalogs have quality issues ;)
		
		String queryStr = "CONSTRUCT {\n" + 
				"        ?a ?b ?c .\n" + 
				"        ?c ?d ?e\n" + 
				"      } {\n" + 
				"\n" + 
				"        { SELECT DISTINCT ?a {\n" + 
				"          ?a dcat:distribution [\n" + 
//				"            dcat:byteSize ?byteSize\n" + 
				"          ]\n" + 
				"        } LIMIT 10 }\n" + 
				"\n" + 
				"        ?a ?b ?c\n" + 
				"        OPTIONAL { ?c ?d ?e }\n" + 
				"}";
		
		Query dcatQuery = parser.apply(queryStr).getAsQueryStmt().getQuery();
	
		
		List<Resource> contexts;
//		try(RdfDataObject catalog = DataObjects.fromSparqlEndpoint("https://databus.dbpedia.org/repo/sparql", null, null)) {			
		try(RdfDataPod catalog = basicWorkflow.accept(executor)) {			
			try(RDFConnection conn = catalog.openConnection()) {
				
	    	    contexts = SparqlRx.execConstructGrouped(conn, Vars.a, dcatQuery)
		    	        .map(RDFNode::asResource)
	    	    		.toList()
	    	    		.blockingGet();

				
//				urls = SparqlRx.execSelect(conn,
////						"SELECT DISTINCT ?o { ?s <http://www.w3.org/ns/dcat#downloadURL> ?o } LIMIT 10")
//						parser.apply("SELECT DISTINCT ?o { ?s dataid:group ?g ; dcat:distribution/dcat:downloadURL ?o } LIMIT 10")
//							.getAsQueryStmt().getQuery())
//					.map(qs -> qs.get("o"))
//					.map(RDFNode::toString)
//					.toList()
//					.blockingGet();				
			}			
		}

		
		// Ready for workflow execution!

		logger.info("Retrieved " + contexts.size() + " contexts for processing " + contexts);
		
		for(Resource context : contexts) {
			
			String url = DcatUtils.getFirstDownloadUrl(context);
			
			logger.info("Processing: " + url);
			RDFNode jobContext = ModelFactory.createDefaultModel().createResource();

			
			// Create a copy of the workflow spec and substitute the variables
			Map<String, Op> map = Collections.singletonMap("dataRef", OpDataRefResource.from(model, DataRefUrl.create(model, url)));			
			Op effectiveWorkflow = OpUtils.copyWithSubstitution(job.getOp(), map::get);			

			
			// Add an initial empty binding
			Multimap<Var, Node> valueMap = LinkedHashMultimap.create();
			
			FunctionAssembler assembler = new FunctionAssembler();
			for(JobBinding bspec : job.getJobBindings()) {
				String varName = bspec.getVarName();
				Var var = Var.alloc(varName);
				OpTraversal trav = bspec.getTraversal();
				
				Function<RDFNode, Set<RDFNode>> fn = trav.accept(assembler);
				
				Set<RDFNode> values = fn.apply(jobContext);
				for(RDFNode value : values) {
					Node node = value.asNode();
					valueMap.put(var, node);
				}
			}

			// Create the set of bindings
			// TODO Is there a nifty way to create the cartesian product with flatMap?
			List<Binding> currentBindings = new ArrayList<>();
			currentBindings.add(BindingFactory.root());

			List<Binding> nextBindings = new ArrayList<>();
			for(Entry<Var, Collection<Node>> e : valueMap.asMap().entrySet()) {
				Var k = e.getKey();
				Collection<Node> vs = e.getValue();
				
				for(Node node : vs) {
					for(Binding cb : currentBindings) {
						Binding nb = BindingFactory.binding(cb, k, node);
						nextBindings.add(nb);
					}
				}
				
				List<Binding> xtmp = currentBindings;
				currentBindings = nextBindings;
				nextBindings = xtmp;
				nextBindings.clear();
			}

			if(currentBindings.isEmpty() || currentBindings.size() > 1) {
				throw new RuntimeException("Can only handle exactly a single binding at present");
			}
			
			Binding binding = currentBindings.iterator().next();
			
			System.out.println("BINDING: " + binding);
			

			// Set up a dataset processing expression		
			logger.info("Conjure spec is:");
			RDFDataMgr.write(System.err, effectiveWorkflow.getModel(), RDFFormat.TURTLE_PRETTY);
			
			try(RdfDataPod data = effectiveWorkflow.accept(executor)) {
				try(RDFConnection conn = data.openConnection()) {
					// Print out the data that is the process result
					Model rmodel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					
					RDFDataMgr.write(System.out, rmodel, RDFFormat.TURTLE_PRETTY);
				}
			} catch(Exception e) {
				logger.warn("Failed to process " + url, e);
			}
		}

	}
}
