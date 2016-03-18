package org.aksw.jena_sparql_api.playground;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api_sparql_path2.JoinSummaryUtils;
import org.aksw.jena_sparql_api_sparql_path2.MainSparqlPath2;
import org.aksw.jena_sparql_api_sparql_path2.PropertyFunctionFactoryKShortestPaths;
import org.aksw.jena_sparql_api_sparql_path2.PropertyFunctionKShortestPaths;
import org.aksw.jena_sparql_api_sparql_path2.SparqlKShortestPathFinderYen;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import au.com.bytecode.opencsv.CSVReader;


public class TaskRunner {



    public static SparqlService createSparqlService(String datasetUri, ResourceLoader resourceLoader, Prologue prologue) throws IOException {
        Resource resource = resourceLoader.getResource(datasetUri);
        InputStream in = resource.getInputStream();
        Model baseDataModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(baseDataModel, in, Lang.TURTLE);

        SparqlService baseDataService = FluentSparqlService
                .from(baseDataModel)
                .create();

        SparqlService predicateJoinSummaryService = FluentSparqlService
                .from(JoinSummaryUtils.createPredicateJoinSummary(baseDataService.getQueryExecutionFactory()))
                .create();

        SparqlService predicateSummaryService = FluentSparqlService
                .from(JoinSummaryUtils.createPredicateSummary(baseDataService.getQueryExecutionFactory()))
                .create();


        SparqlStmtParserImpl sparqlStmtParser = SparqlStmtParserImpl.create(SparqlParserConfig.create(Syntax.syntaxARQ, prologue));


        SparqlService result = MainSparqlPath2.proxySparqlService(baseDataService, sparqlStmtParser, prologue);

        return result;
    }


    public static TaskContext createTaskContext(List<String> cols, ResourceLoader resourceLoader, String basePath) throws IOException {

        // TODO: The mapping from sparql service to path finder must be made configurable
        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths(ss ->
            new SparqlKShortestPathFinderYen(ss.getQueryExecutionFactory())
        ));


        PrefixMappingImpl pm = new PrefixMappingImpl();
        pm.setNsPrefix("jsafn", "http://jsa.aksw.org/fn/");
        pm.setNsPrefixes(PrefixMapping.Extended);
        Prologue prologue = new Prologue(pm);



        TaskContext result = new TaskContext();

        result.setDescription("Task: " + cols);

        if(cols.size() != 6) {
            throw new RuntimeException("Input rows must have exactly 6 columns, got :" + cols.size() + ": " + cols);
        }

        String dataset = cols.get(0);
        SparqlService ss = createSparqlService(dataset, resourceLoader, prologue);
        result.setSparqlService(ss);

        Node start = NodeFactory.createURI(cols.get(1));
        Node end = NodeFactory.createURI(cols.get(2));

        result.setStartNode(start);
        result.setEndNode(end);

        int k = Integer.parseInt(cols.get(3));
        result.setK(k);


        result.setPath(PathParser.parse(cols.get(4), prologue.getPrefixMapping()));

        String refFile = cols.get(5);
        ClassPathResource cp = new ClassPathResource(basePath + "/" + refFile);
        if(!cp.exists()) {
            throw new RuntimeException("Reference file does not exist: " + cp);
        }



        System.out.println(cols);
        System.out.println(dataset);

        return result;

        //        Stopwatch sw = Stopwatch.createStarted();
//
//        //joinSummaryModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/Spark-RDF/tmp/fp7-summary-predicate-join.nt");
//        //System.out.println("Join Summary Read took: " + sw.stop().elapsed(TimeUnit.SECONDS) + " for " + joinSummaryModel.size() + " triples");
//
//        Model model = ModelFactory.createDefaultModel();
//        //RDFDataMgr.read(model, "classpath://dataset-fp7.ttl");
//        RDFDataMgr.read(model, (new ClassPathResource("dataset-fp7.ttl").getInputStream()), Lang.TTL);
//
//        Resource ds = ResourceFactory.createResource("http://example.org/resource/data-fp7");

        //String q = "Select ?service ?graph"

//
//        dataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/");
//        predDataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/summary/predicate/");
//        predJoinDataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/summary/predicate-join/");
//
//        desiredPred = NodeFactory.createURI("http://fp7-pp.publicdata.eu/ontology/funding");
//        pathExprStr = createPathExprStr("http://fp7-pp.publicdata.eu/ontology/funding");
//        //pathExprStr = "<http://fp7-pp.publicdata.eu/ontology/funding>/^<http://foo>/<http://fp7-pp.publicdata.eu/ontology/funding>/<http://fp7-pp.publicdata.eu/ontology/partner>/!<http://foobar>*";
//        startNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/257943");
//        endNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/city/France-PARIS");
//
//        queryStr = "SELECT ?path { <" + startNode.getURI() + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <" + endNode.getURI() + "> 471199) }";

    }


    public static void runTask(TaskContext taskContext) {
        String pathExprStr = "" +  taskContext.getPath();
        Node startNode = taskContext.getStartNode();
        Node endNode = taskContext.getEndNode();
        String queryStr = "SELECT ?path { <" + startNode.getURI() + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <" + endNode.getURI() + "> 471199) }";

        QueryExecutionFactory qef = taskContext.getSparqlService().getQueryExecutionFactory();
        QueryExecution qe = qef.createQueryExecution(queryStr);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            System.out.println("Binding: " + binding);
        }
    }

    public static void main(String[] args) throws IOException {

        ResourceLoader resourceLoader = new AnnotationConfigApplicationContext();

//        URL url = new URL("classpath:custom/data.nt");
//        System.out.println(StreamUtils.toString(url.openStream()));

        Map<String, QueryExecutionFactory> datasetToQef = new HashMap<>();

        List<TaskContext> taskContexts = new ArrayList<>();

        String basePath = "custom";
        try(CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(basePath + "/tasks.tsv").getInputStream()), '\t')) {

            // First path: Validate referenecd resources and set up services
            String[] row;
            while((row = reader.readNext()) != null) {
                List<String> cols = Arrays.asList(row);
                TaskContext taskContext = createTaskContext(cols, resourceLoader, basePath);
                taskContexts.add(taskContext);

            }
        }

        // run the tasks
        for(TaskContext taskContext : taskContexts) {
            runTask(taskContext);
            //taskContext.get
        }

    }
}
