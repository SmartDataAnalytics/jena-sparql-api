package org.aksw.jena_sparql_api.conjure.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ConjureFormatConfig;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilder;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureContext;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureFluent;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.fluent.QLib;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobBinding;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.io.json.RDFNodeJsonUtils;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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



    public static Object execute(JobInstance jobInstance) {



        return null;
    }





    public static void main2(String[] args) {

        String url = "http://localhost/~raven/test.hdt";


//		ConjureContext ctx = new ConjureContext();
//		Model model = ctx.getModel();
//
        ConjureBuilder cj = new ConjureBuilderImpl();


        Op op = cj.coalesce(
                cj.fromUrl(url).hdtHeader().construct("CONSTRUCT WHERE { ?s <urn:tripleCount> ?o }"),
                cj.fromUrl(url).tripleCount().cache()).getOp();

        Model model = cj.getContext().getModel();
        Job job = Job.create(model);
        job.setOp(op);
        job.getJobBindings().add(JobBinding.create(model, "datasetId", OpTraversalSelf.create(model)));

        // Goal: spark-submit-cj.sh catalog macrolib macroname
//		cj.call("myMacro", cj.fromUrl(url));

        RDFDataMgr.write(System.out, job.getModel(), RDFFormat.TURTLE_PRETTY);
    }

    public static void main(String[] args) throws Exception {
        JenaSystem.init();
//		fromSparqlFile("/home/raven/Projects/limbo/git/poi-rostock/dcat.sparql");
        Job job = JobUtils.fromSparqlFile("replacens.sparql");

        Map<String, Node> env = ImmutableMap.<String, Node>builder()
                .put("SOURCE_NS", NodeFactory.createLiteral("https://portal.limbo-project.org"))
                .put("TARGET_NS", NodeFactory.createLiteral("https://data.limbo-project.org"))
                .build();

        Op op = ConjureBuilderImpl.start().fromUrl("file:///home/raven/Projects/limbo/git/train_2-dataset/train_2-dataset.ttl").getOp();
        //DataRef dataRef = DataRefUrl.create(ModelFactory.createDefaultModel(), "http://foo.bar/baz");

        Map<String, Op> map = Collections.singletonMap("ARG", op);

        System.out.println("Op Vars: " + job.getOpVars());
        System.out.println("Literal Vars: " + job.getDeclaredVars());

        JobInstance ji = JobUtils.createJobInstanceWithCopy(job, env, map);

        System.out.println("EnvMap: " + ji.getEnvMap());
        System.out.println("OpMap: " + ji.getOpVarMap());


//		RDFDataMgr.write(System.out, ji.getModel(), RDFFormat.TURTLE_PRETTY);
        Op inst = JobUtils.materializeJobInstance(ji);
        RDFDataMgr.write(System.out, inst.getModel(), RDFFormat.TURTLE_PRETTY);


        try(RdfDataPod pod = ExecutionUtils.executeJob(inst)) {
            Model m = pod.getModel();
            System.out.println("Transformed Model:");
            RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
        }

        // TODO Parse out all <env:> Nodes from the environment...
        //Job job = Job.create(cj.getContext().getModel(), "todoJobName");
        //JobInstance inst = createJobInstance(job, env, map);

        if(true) {
            return;
        }
        /*
        HashCode a = Hashing.sha256().hashString("a", StandardCharsets.UTF_8);
        HashCode b = Hashing.sha256().hashString("b", StandardCharsets.UTF_8);
        HashCode c = Hashing.sha256().hashString("c", StandardCharsets.UTF_8);

        String x = Hashing.combineUnordered(Arrays.asList(a, b, c)).toString();
        String y = Hashing.combineUnordered(Arrays.asList(a, Hashing.combineUnordered(Arrays.asList(b, c)))).toString();
        String z = Hashing.combineUnordered(Arrays.asList(b, Hashing.combineUnordered(Arrays.asList(c, a)))).toString();

        System.out.println("x: " + x);
        System.out.println("y: " + y);
        System.out.println("z: " + z);

        if(true) {
            return;
        }
        */

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

        Op coreOp = OpConstruct.create(model, v, parser.apply(
                  "CONSTRUCT {\n" +
                  "	    	          <env:datasetId> <urn:count> ?c\n" +
                  "	    	        } {\n" +
                  "	    	          { SELECT (COUNT(*) AS ?c) {\n" +
                  "	    	            ?s ?p ?o\n" +
                  "	    	          } }\n" +
                  "	    	        }").toString());

        conjureWorkflow = OpPersist.create(model, coreOp);


        Op test = OpUtils.stripCache(conjureWorkflow);

        String origHash = ResourceTreeUtils.createGenericHash(conjureWorkflow).toString();
        String coreHash = ResourceTreeUtils.createGenericHash(coreOp).toString();
        String cleanedHash = ResourceTreeUtils.createGenericHash(test).toString();

        // Assertion: cleaned hash == orig hash
        System.out.println("ORIG HASH: " + origHash);
        System.out.println("CORE HASH: " + coreHash);
        System.out.println("CLEANED HASH: " + cleanedHash);


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

        if(false) {
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
        }

        if(false) {
        conjureWorkflow =
                cj.seq(
                    cj.fromUrl("http://input").set("DATAID", "SELECT ?x { ?x dcat:distribution [] }", null),
                    dataset.construct("CONSTRUCT { ?DATAID a <urn:Report> ; <urn:usesProperty> ?p } { BIND(BNODE() AS ?b) { SELECT DISTINCT ?p { ?s ?p ?o } } }")
                )
                    .getOp();
        }

        conjureWorkflow = dataset.hdtHeader().everthing().cache()
                    .getOp();


        Job testJob = Job.create(xmodel, "unnamedJob");
        testJob.setOp(conjureWorkflow);
        testJob.setJobBindings(Arrays.asList(JobBinding.create(xmodel, "datasetId", OpTraversalSelf.create(xmodel))));




        // Print out the deserialized workflow for inspection
        RDFDataMgr.write(System.err, testJob.getModel(), RDFFormat.TURTLE_PRETTY);

//		if(true) {
//			return;
//		}

        // In case you missed it because you couldn't see it: The workflow *IS* RDF:
        // We created RDF using static factory methods, saved it to disk and loaded it again


        // Set up a simple file-system based conjure repository and workflow executor
        // This does HTTP caching, content type conversion and content negotiation
        // Lots of magic, fairies and unicorns in there
        // (and gears and screws one wants to configure for production use - and which may at this stage sometimes break)
        HttpResourceRepositoryFromFileSystemImpl repo = HttpResourceRepositoryFromFileSystemImpl.createDefault();
        ResourceStore cacheStore = repo.getCacheStore();
        OpExecutorDefault catalogExecutor = new OpExecutorDefault(repo, null, new LinkedHashMap<>(), RDFFormat.TURTLE_PRETTY);

        // Get a copy of the limbo dataset catalog via the repo so that it gets cached
        DataRef dataRef1 = DataRefUrl.create(model, "https://gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl");

        // Or set up a workflow that makes databus available
        DataRef dataRef2 = DataRefSparqlEndpoint.create(model, "https://databus.dbpedia.org/repo/sparql");


        // Create a data ref from a workflow
        DataRef dataRef3 = DataRefOp.create(
                OpUpdateRequest.create(model, OpData.create(model),
//					parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
                        parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <http://localhost/~raven/bib_lds_20190305.hdt.gz> ] }").toString()));

        DataRef dataRef4 = DataRefOp.create(
                OpUpdateRequest.create(model, OpData.create(model),
//					parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
                        parser.apply("INSERT DATA { <http://mydata> dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <https://data.dnb.de/opendata/zdb_lds.hdt.gz> ] }").toString()));


            DataRef dataRef5 = DataRefOp.create(
                    OpUpdateRequest.create(model, OpData.create(model),
//						parser.apply("INSERT DATA { [] dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <file:///home/raven/tmp/test.hdt> ] }").toString()));
                            parser.apply("INSERT DATA { <http://mydata> dataid:group eg:mygrp ; dcat:distribution [ dcat:downloadURL <http://localhost/~raven/009e80050fa7f4279596956477157ec2.hdt> ] }").toString()));

        DataRef dataRefX = dataRef4;

        // Set up the workflow that makes a digital copy of a dataset available
        Op basicWorkflow = OpDataRefResource.from(model, dataRefX);

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


        List<TaskContext> taskContexts = new ArrayList<>();
        //List<Resource> inputRecords;
//		try(RdfDataObject catalog = DataObjects.fromSparqlEndpoint("https://databus.dbpedia.org/repo/sparql", null, null)) {
        try(RdfDataPod catalog = basicWorkflow.accept(catalogExecutor)) {
            try(RDFConnection conn = catalog.openConnection()) {

                List<Resource> inputRecords = SparqlRx.execConstructGrouped(conn, dcatQuery, Vars.a)
                        .map(RDFNode::asResource)
                        .toList()
                        .blockingGet();

                // For every input record is a dcat entry, assign an anonymous dataref
                for(Resource inputRecord : inputRecords) {
                    Map<String, Op> nameToDataRef = new HashMap<>();

                    Query q = parser.apply("SELECT DISTINCT ?x { ?x dcat:distribution [] }").getQuery();
                    Model m = inputRecord.getModel();

                    // QueryExecution qe =

                    List<Resource> dcatDataRefs = SparqlRx.execSelect(() -> QueryExecutionFactory.create(q, m))
                        .map(qs -> qs.get("x"))
                        .map(RDFNode::asResource)
                        .toList()
                        .blockingGet();

                    int i = 0;
                    for(Resource r : dcatDataRefs) {
                        Model xxmodel = ModelFactory.createDefaultModel();
                        xxmodel.add(r.getModel());
                        r = r.inModel(xxmodel);

                        DataRefDcat dr = DataRefDcat.create(xxmodel, r);
                        Op odr = OpDataRefResource.from(dr.getModel(), dr);

                        RDFDataMgr.write(System.err, dr.getModel(), RDFFormat.TURTLE_PRETTY);

                        nameToDataRef.put("unnamedDataRef" + (i++), odr);
                    }

                    logger.info("Registered data refs for input " + inputRecord + " are: " + nameToDataRef);
                    Map<String, Model> nameToModel = new HashMap<>();
                    nameToModel.put("http://input", inputRecord.getModel());

                    TaskContext taskContext = new TaskContext(inputRecord, nameToDataRef, nameToModel);
                    taskContexts.add(taskContext);
                    // Note, that the dcat ref query was run on the inputContext models
                    // So the following assertion is assumed to hold:
                    // dcatDataRef.getModel() == inputRecord.getModel()
                }

                logger.info("Created " + taskContexts.size() + " task contexts");

//	    		if(true) {
//	    			return;
//	    		}

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


        // Check the contexts for well-known data refs; i.e. dcat entries




        // Ready for workflow execution!

//		logger.info("Retrieved " + inputRecords.size() + " contexts for processing " + inputRecords);
        for(TaskContext taskContext : taskContexts) {
            ExecutionUtils.executeJob(testJob, taskContext, repo, cacheStore, new ConjureFormatConfig());
        }

    }

}
