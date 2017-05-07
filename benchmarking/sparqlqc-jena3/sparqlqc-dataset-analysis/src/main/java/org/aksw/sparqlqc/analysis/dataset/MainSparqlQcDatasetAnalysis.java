package org.aksw.sparqlqc.analysis.dataset;

import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.shape.lookup.MapServiceResourceShape;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Range;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class MainSparqlQcDatasetAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(MainSparqlQcDatasetAnalysis.class);

    // PropertyTester calls System.exit(...) because projections are not supported...


    private static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkExit(int arg0) {
                throw new SecurityException("Exit trapped");
            }

            @Override
            public void checkPermission(Permission permission) {
                if (permission.getName().startsWith("exitVM")) {
                    throw new SecurityException("Exit trapped");
                }
            }

        };
        System.setSecurityManager(securityManager);
    }

    private static void enableSystemExitCall() {
        System.setSecurityManager(null);
    }

    public static void main(String[] args) throws Exception {

        if(true) {
            filterByIdenticalNormalizedQuery();
            return;
        }


        OptionParser parser = new OptionParser();

        String endpointUrl = "http://localhost:8890/sparql";
        String queryStr = "PREFIX lsq:<http://lsq.aksw.org/vocab#> SELECT ?s ?o { ?s lsq:text ?o ; lsq:hasSpin [ a <http://spinrdf.org/sp#Select> ] } ORDER BY ASC(strlen(?o))";

        boolean useParallel = false;

        OptionSpec<String> endpointUrlOs = parser
                .acceptsAll(Arrays.asList("e", "endpoint"), "Local SPARQL service (endpoint) URL on which to execute queries")
                .withRequiredArg()
                .defaultsTo(endpointUrl)
                ;

        OptionSpec<String> graphUriOs = parser
                .acceptsAll(Arrays.asList("g", "graph"), "Local graph(s) from which to retrieve the data")
                .withRequiredArg()
                ;

        OptionSpec<String> queryOs = parser
                .acceptsAll(Arrays.asList("q", "query"), "Query for fetching query resources and strings, output variables must be named ?s and ?o")
                .withRequiredArg()
                .defaultsTo(queryStr)
                ;

        OptionSpec<Integer> nOs = parser
                .acceptsAll(Arrays.asList("n", "numItems"), "Number of items to process")
                .withRequiredArg()
                .ofType(Integer.class)
                ;

        OptionSpec<String> parallelOs = parser
                .acceptsAll(Arrays.asList("p", "parallel"), "Use parallel stream processing - may not work with certain solvers")
                .withOptionalArg()
                //.ofType(Boolean.class)
                ;


        OptionSet options = parser.parse(args);


        forbidSystemExitCall();
        FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

        Map<String, String> config = new HashMap<String, String>();
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");

        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",",
                // API
                "fr.inrialpes.tyrexmo.testqc.simple;version=\"1.0.0\"", "fr.inrialpes.tyrexmo.testqc;version=\"1.0.0\"",

                // Dirty API packages (probably should go elsewhere)
                "fr.inrialpes.tyrexmo.queryanalysis;version=\"1.0.0\"",

                // Jena 3
                "org.apache.jena.sparql.algebra;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.optimize;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.op;version=\"1.0.0\"",
                "org.apache.jena.sparql.algebra.expr;version=\"1.0.0\"",
                "org.apache.jena.sparql.core;version=\"1.0.0\"", "org.apache.jena.sparql.syntax;version=\"1.0.0\"",
                "org.apache.jena.sparql.expr;version=\"1.0.0\"", "org.apache.jena.sparql.graph;version=\"1.0.0\"",
                "org.apache.jena.query;version=\"1.0.0\"", "org.apache.jena.graph;version=\"1.0.0\"",
                "org.apache.jena.ext.com.google.common.collect;version=\"1.0.0\"",
                "org.apache.jena.sparql.engine.binding;version=\"1.0.0\"", "org.apache.jena.atlas.io;version=\"1.0.0\"",

                // Jena 2 (legacy)
                "com.hp.hpl.jena.sparql;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.algebra;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.algebra.optimize;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.algebra.op;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.algebra.expr;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.engine;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.core;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.syntax;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.expr;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.expr.nodevalue;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.graph;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.util.graph;version=\"1.0.0\"",
                "com.hp.hpl.jena.query;version=\"1.0.0\"", "com.hp.hpl.jena.graph;version=\"1.0.0\"",
                "com.hp.hpl.jena.ext.com.google.common.collect;version=\"1.0.0\"",
                "com.hp.hpl.jena.sparql.engine.binding;version=\"1.0.0\"",

                "org.apache.xerces.util;version=\"1.0.0\"", "org.apache.xerces.impl.dv;version=\"1.0.0\"",
                "org.apache.xerces.xs;version=\"1.0.0\"", "org.apache.xerces.impl.dv.xs;version=\"1.0.0\"",
                "org.apache.xerces.impl.validation;version=\"1.0.0\"",

                "com.ibm.icu.text;version=\"1.0.0\"",

                // Logging
                "org.slf4j;version=\"1.7.0\""
        // "org.slf4j.impl;version=\"1.0.0\"",
        // "org.apache.log4j;version=\"1.0.0\""

        // ??? What packages are that?
        // "java_cup.runtime;version=\"1.0.0\""
        ));

        Framework framework = frameworkFactory.newFramework(config);
        framework.init();
        framework.start();


        try {

            if(options.has(endpointUrlOs)) {
                endpointUrl = endpointUrlOs.value(options);
            }

            List<String> defaultGraphIris = graphUriOs.values(options);
            if(options.has(queryOs)) {
                queryStr = queryOs.value(options);
            }
            Integer n = nOs.value(options);

            if(n != null) {
                queryStr = queryStr + " LIMIT " + n;
            }

            useParallel = options.has(parallelOs);


            DatasetDescription datasetDescription = DatasetDescription.create(defaultGraphIris, Collections.emptyList());
            SparqlServiceReference endpointDescription = new SparqlServiceReference(endpointUrl, datasetDescription);

            logger.info("Endpoint: " + endpointDescription);
            logger.info("Query: " + queryStr);
            logger.info("Parallel: " + useParallel);

            QueryExecutionFactory qef = FluentQueryExecutionFactory.http(endpointDescription).create();
            QueryExecution qe = qef.createQueryExecution(queryStr);
//            QueryExecution qe = qef.createQueryExecution(
//                    "PREFIX lsq:<http://lsq.aksw.org/vocab#> CONSTRUCT { ?s lsq:text ?o } { ?s lsq:text ?o ; lsq:hasSpin [ a <http://spinrdf.org/sp#Select> ] } ORDER BY ASC(strlen(?o)) LIMIT 3000");

            Model in = ModelFactory.createDefaultModel();
            qe.execSelect().forEachRemaining(qs -> {
                in.add(qs.get("s").asResource(), LSQ.text, qs.get("o"));
            });

            Set<Resource> items = in.listSubjects().toSet();

            Supplier<Stream<Resource>> queryIterable = useParallel
                    ? () -> items.parallelStream()
                    : () -> items.stream();



            run(framework, queryIterable);
        } catch(Exception e) {
            e.printStackTrace();
        }

        framework.stop();
        if (false) {
            framework.waitForStop(0);
        } else {
            enableSystemExitCall();
            System.exit(0);
        }

    }


    /**
     * - Gather a chunk of triples from the stream,
     * - Create the vertexset, i.e. the set of subjects / objects (we could use the jgraphT pseudograph wrapper for that)
     * - Perform lookup of the query strings
     * - Parse the query strings (might use a cache)
     * - Remove prefixes
     * - Check for equivalence
     * - If equal, filter the triple, i.e. don't output it
     *
     * @param tripleStream
     * @param qef
     */
    public static void filterByIdenticalNormalizedQuery() {//Stream<Triple> tripleStream, QueryExecutionFactory qef) {
        String fileUrl = "file:///home/raven/Downloads/result.nt";
        //Model model = RDFDataMgr.loadModel("file:///home/raven/Downloads/result.nt");
        //QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model).create();

        QueryExecutionFactory qef = FluentQueryExecutionFactory.fromFileNameOrUrl(fileUrl).create();

        SparqlFlowEngine engine = new SparqlFlowEngine(qef);

        engine
            .fromSelect("SELECT * { ?s ?p ?o }")
            .chunk(10).apply(Range.atMost(10l)).forEach(x -> {
                System.out.println("got batch: " + x);
            });

        if(true) {
            return;
        }


        QueryExecutionFactory dataQef = FluentQueryExecutionFactory.http("http://localhost:8950/sparql").create();

        ResourceShapeBuilder linkRsb = new ResourceShapeBuilder();
        linkRsb.out("http://lsq.aksw.org/vocab#isEntailed-JSAC");


        ResourceShape shape = linkRsb.getResourceShape();
        ListService<Concept, Resource> ms = MapServiceResourceShape.createListService(qef, shape, true);


        ResourceShapeBuilder dataRsb = new ResourceShapeBuilder();
        dataRsb.out(LSQ.text);
        ResourceShape dataShape = linkRsb.getResourceShape();


        //LookupService<Node, Resource> nodeToQuery =
        LookupService<Node, Resource> ls = MapServiceResourceShape.createLookupService(dataQef, dataShape)
                    .mapValues(ResourceUtils::asResource);

        //ls.apply(t);

        //ls.fe


        //LookupServiceUtils.createLookupService(dataQef, dataShape);


        //feed(ms.streamData(null, null))

        SparqlQueryParser parser = SparqlQueryParserImpl.create();
        //LookupServiceListService.create(listService)
        ms.streamData(null, null).forEach(r -> {
            Set<Node> nodes = new HashSet<>();
            nodes.add(r.asNode());
            Set<Node> targets = r.listProperties().mapWith(Statement::getObject).mapWith(RDFNode::asNode).toSet();
            nodes.addAll(targets);

            Map<Node, Query> map = ls.apply(nodes).entrySet().stream().collect(
                    Collectors.toMap(e -> e.getKey(), e -> parser.apply(e.getValue().getProperty(LSQ.text).getString())));





            System.out.println("Resource: " + map);
            //RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS);
        });



        //shape.


        //ParameterizedSparqlString pss = new ParameterizedSparqlString();

        //GraphUtils.allNodes(graph)

        //LookupServiceSparqlQuery
    //	qef.createQueryExecution("CONSTRUCT { ?s lsq:asText ?o } WHERE { ?s lsq:asText)
    //	tripleStream.forEach(
    }



    public static void run(Framework framework, Supplier<Stream<Resource>> queryIterable) throws Exception {



        List<String> pluginNames = Arrays.asList("jsa"); //, "sparqlalgebra", "afmu", "treesolver");

        // "reference:file:" + jarFileStr
        List<String> jarRefs = pluginNames.stream().map(pluginName ->
            String.format("sparqlqc-impl-%1$s-1.0.0-SNAPSHOT.jar", pluginName)
        ).collect(Collectors.toList());


        BundleContext context = framework.getBundleContext();

        for (String jarRef : jarRefs) {
            logger.info("Loading: " + jarRef);
            ClassPathResource r = new ClassPathResource(jarRef);

            Bundle bundle = context.installBundle("inputstream:" + jarRef, r.getInputStream());
            bundle.start();
        }

        ServiceReference<?>[] srs = context.getAllServiceReferences(SimpleContainmentSolver.class.getName(), null);

        Map<String, SimpleContainmentSolver> solvers = Arrays.asList(srs).stream().collect(Collectors.toMap(
                sr -> "" + sr.getProperty("SHORT_LABEL"), sr -> (SimpleContainmentSolver) context.getService(sr)));

        //System.out.println(solvers);

        // QueryExecutionFactory qef = FluentI


        // String xxx = "PREFIX lsq:<http://lsq.aksw.org/vocab#> CONSTRUCT { ?s
        // ?p ?o } WHERE { { SELECT SAMPLE(?x) AS ?s (lsq:text As ?p) ?o { ?x
        // lsq:text ?o . } GROUP BY ?o } } LIMIT 10";

        // Model in = ModelFactory.createDefaultModel();
        // in.createResource("http://ex.org/foo")
        // .addLiteral(LSQ.text, "SELECT * { ?s ?p ?o }");
        //
        // in.createResource("http://ex.org/bar")
        // .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z }");
        //
        // in.createResource("http://ex.org/baz")
        // .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z . FILTER(?x = <test>)}");

        // Supplier<Stream<Resource>> queryIterable = () ->
        // in.listSubjects().toSet().parallelStream();

        // System.out.println(solvers);
        // if(true) return;

        // Entry<String, SimpleContainmentSolver> e =
        // solvers.entrySet().iterator().next();
        // String solverShortLabel = e.getKey();
        // SimpleContainmentSolver solver = e.getValue();

        String solverShortLabel = "JSAC";
        SimpleContainmentSolver solver = solvers.get(solverShortLabel);

        String ns = "http://lsq.aksw.org/vocab#";
        Property _isEntailed = ResourceFactory.createProperty(ns + "isEntailed-" + solverShortLabel);
        Property _isNotEntailed = ResourceFactory.createProperty(ns + "isNotEntailed-" + solverShortLabel);
        Property _entailmentError = ResourceFactory.createProperty(ns + "entailmentError-" + solverShortLabel);
        Property _inputError = ResourceFactory.createProperty(ns + "inputError-" + solverShortLabel);

        Stream<Statement> x = queryIterable.get()//.peek((foo) -> System.out.println("foo: " + foo))
                .flatMap(a -> queryIterable.get().map(b -> {
                    Model m = ModelFactory.createDefaultModel();
                    try {
//                        String tmp = a + " --- " + b;
//                        System.out.println(tmp);
//                        if(tmp.equals("http://lsq.aksw.org/res/q-f460c9be --- http://lsq.aksw.org/res/q-ef29c588")) {
//                            System.out.println("here");
//                        }

                        String aStr = a.getProperty(LSQ.text).getString();
                        String bStr = b.getProperty(LSQ.text).getString();

                        try {
                            boolean isEntailed = solver.entailed(aStr, bStr);
                            if (isEntailed) {
                                m.add(a, _isEntailed, b);
                            } else {
                                m.add(a, _isNotEntailed, b);
                            }
                        } catch (Exception ex) {
                            m.add(a, _entailmentError, b);
                            logger.warn("Entailment error", ex);
                        }
                    } catch (Exception ey) {
                        m.add(a, _inputError, b);
                        logger.warn("Input error", ey);
                    }
                    return m.listStatements().next();
                })

        );

        // x.forEach(System.out::println);

        // System.out.println(x.count());

        //OutputStream out = new FileOutputStream(new File("/mnt/Data/tmp/swdf-containment-" + solverShortLabel + ".nt"));
        x.forEach(stmt -> {
            Model m = ModelFactory.createDefaultModel();
            m.add(stmt);
            RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES_UTF8);
        });

        //out.flush();
        //out.close();


    }

    // public boo testContain(Resource a, Resource b)
}
