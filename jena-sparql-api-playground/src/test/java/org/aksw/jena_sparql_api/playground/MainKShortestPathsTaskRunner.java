package org.aksw.jena_sparql_api.playground;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api_sparql_path2.PropertyFunctionFactoryKShortestPaths;
import org.aksw.jena_sparql_api_sparql_path2.PropertyFunctionKShortestPaths;
import org.aksw.jena_sparql_api_sparql_path2.SparqlKShortestPathFinderYen;
import org.aksw.jena_sparql_api_sparql_path2.SparqlPathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
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


public class MainKShortestPathsTaskRunner {




    public static TaskContext createTaskContext(List<String> cols, ResourceLoader resourceLoader, String basePath, QueryExecutionFactory dcatQef) throws IOException {

        // TODO: The mapping from sparql service to path finder must be made configurable
        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths(ss ->
            new SparqlKShortestPathFinderYen(ss.getQueryExecutionFactory(), 50)
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
        SparqlService ss = SparqlPathUtils.createSparqlService(dataset, resourceLoader, prologue, dcatQef);
        result.setSparqlService(ss);

        Node start = NodeFactory.createURI(cols.get(1));
        Node end = NodeFactory.createURI(cols.get(2));

        result.setStartNode(start);
        result.setEndNode(end);

        int k = Integer.parseInt(cols.get(3));
        result.setK(k);


        String rawPathStr = cols.get(4).trim();
        String pathStr;
        if(rawPathStr.isEmpty()) {
            //pathStr = "(<http://ex.org/p>|!<http://ex.org/p>)*";
            pathStr = "(!<http://ex.org/tmp>)*";
        }
        else if(rawPathStr.startsWith("http://")) {
            pathStr
                = "<" + rawPathStr + ">/(!<http://ex.org/tmp>)*|"
                + "(!<http://ex.org/tmp>)/<" + rawPathStr + ">" ;
        }
        else {
            pathStr = rawPathStr;
        }

        result.setPath(PathParser.parse(pathStr, prologue.getPrefixMapping()));

        //System.out.println("GOT PATH: " + result.getPath());

        String refFile = cols.get(5);
        Resource cp = resourceLoader.getResource(basePath + "/" + refFile);
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


    public static void runTask(TaskContext taskContext) throws IOException {
        System.out.println("Running task " + taskContext.getStartNode() + " " + taskContext.getEndNode() + " " + taskContext.getK());
        String pathExprStr = "" +  taskContext.getPath();
        Node startNode = taskContext.getStartNode();
        Node endNode = taskContext.getEndNode();
        String queryStr = "SELECT ?path { <" + startNode.getURI() + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <" + endNode.getURI() + "> " + taskContext.getK() + ") }";


        QueryExecutionFactory qef = taskContext.getSparqlService().getQueryExecutionFactory();
        QueryExecution qe = qef.createQueryExecution(queryStr);
        ResultSet rs = qe.execSelect();

        File tmpFile = File.createTempFile("path", "txt");
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            System.out.println("Binding: " + binding);
        }

        //throw new RuntimeException("done");
    }

    public static void main(String[] args) throws IOException {

        ResourceLoader resourceLoader = new AnnotationConfigApplicationContext();


        Model datasetModel = ModelFactory.createDefaultModel();
        QueryExecutionFactory dcatQef = FluentQueryExecutionFactory.model(datasetModel).create();
        SparqlPathUtils.readModel(datasetModel, resourceLoader, "classpath:dcat-eswc-training.ttl", Lang.TURTLE);


        //datasetModel.write(System.out, "TTL");


//        URL url = new URL("classpath:custom/data.nt");
//        System.out.println(StreamUtils.toString(url.openStream()));

        Map<String, QueryExecutionFactory> datasetToQef = new HashMap<>();

        List<TaskContext> taskContexts = new ArrayList<>();
        String basePath = "file:///home/raven/Downloads/eswc";
        String taskResource = "eswc-training-task1.tsv";


        String refPath = basePath + "/challenge_training_result_sets";

        //String basePath = "custom";
        //String taskFile = "tasks.tsv";

        //ClassPathResource
        try(CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(taskResource).getInputStream()), '\t')) {

            // First path: Validate referenecd resources and set up services
            String[] row;
            while((row = reader.readNext()) != null) {
                List<String> cols = Arrays.asList(row);

                boolean isRowEmpty = cols.size() == 1 && cols.get(0).trim().equals("");
                if(!isRowEmpty) {
                    TaskContext taskContext = createTaskContext(cols, resourceLoader, refPath, dcatQef);
                    taskContexts.add(taskContext);
                }

            }
        }

        // run the tasks
        for(TaskContext taskContext : taskContexts) {
            runTask(taskContext);

            //break;
            //taskContext.get
        }

    }
}
