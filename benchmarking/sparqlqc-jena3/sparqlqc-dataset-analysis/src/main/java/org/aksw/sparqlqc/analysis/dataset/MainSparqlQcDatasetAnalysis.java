package org.aksw.sparqlqc.analysis.dataset;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.SparqlFlowEngine;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.shape.lookup.MapServiceResourceShape;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import fr.inrialpes.tyrexmo.testqc.SparqlQcTools;
import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class MainSparqlQcDatasetAnalysis {

    static final Logger logger = LoggerFactory.getLogger(MainSparqlQcDatasetAnalysis.class);

    // PropertyTester calls System.exit(...) because projections are not supported...


    public static void main(String[] args) throws Exception {

        SparqlQcTools.init();

//        if(true) {
//            filterByIdenticalNormalizedQuery();
//
//        destroy();
//        System.exit(0);
//            return;
//        }


        OptionParser parser = new OptionParser();

        String endpointUrl = "http://localhost:8890/sparql";
        String queryStr = "PREFIX lsq:<http://lsq.aksw.org/vocab#> SELECT ?s ?o { ?s lsq:text ?o ; lsq:hasSpin [ a <http://spinrdf.org/sp#Select> ] } ORDER BY ASC(strlen(?o))";

        boolean useParallel = false;

        OptionSpec<String> solverOs = parser
                .acceptsAll(Arrays.asList("s", "solver"), "The solver to use, one of: " + SparqlQcTools.solvers.keySet())
                .withRequiredArg()
                //.defaultsTo(null)
                ;

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

            Set<Resource> rawLsqQueries = in.listSubjects().toSet();

            Function<String, Query> queryParser = SparqlQueryParserImpl.create();
            
            Set<Resource> lsqQueries = rawLsqQueries.stream().filter(lsqq -> {
            	boolean r = true;
            	try {
                    Query query = queryParser.apply(lsqq.getProperty(LSQ.text).getString());
                    query.getPrefixMapping().clearNsPrefixMap();
                    // remove queries with values

                    if(!query.isSelectType()) {
                    	r = false;
                    } else {
                    	// Check for ?s ?p ?o queries
	                    Element element = query.getQueryPattern();
	                    if(element instanceof ElementTriplesBlock) {
	                        List<Triple> triples = ((ElementTriplesBlock)element).getPattern().getList();
	
	                        if(triples.size() == 1) {
	
	                            Triple triple = triples.get(0);
	
	                            // TODO Refactor into e.g. ElementUtils.isVarsOnly(element)
	                            boolean condition =
	                                    triple.getSubject().isVariable() &&
	                                    triple.getPredicate().isVariable() &&
	                                    triple.getObject().isVariable();
	                            
	                            if(condition) {
	                            	r = false;
	                            }
	                        }
	                    }
                    }

                    return r;

            	} catch(Exception e) {
            		logger.warn("Could not handle query: " + e);
            	}
            	
            	return r;            	
            }).collect(Collectors.toSet());
            
            
            logger.info("Number of queries remaining: " + lsqQueries.size());            
            TimeUnit.SECONDS.sleep(3);
            
            Supplier<Stream<Resource>> queryIterable = useParallel
                    ? () -> lsqQueries.parallelStream()
                    : () -> lsqQueries.stream();


            String solverLabel = solverOs.value(options);
            SimpleContainmentSolver solver = SparqlQcTools.solvers.get(solverLabel);

            if(solver == null) {
                throw new RuntimeException("No solver with label '" + solverLabel + "' found");
            }

            run(solverLabel, solver, queryIterable);
        } catch(Exception e) {
            e.printStackTrace();
            parser.printHelpOn(System.err);
        }

        SparqlQcTools.destroy();
        System.exit(0);
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
     * @throws IOException
     */
    public static void filterByIdenticalNormalizedQuery() throws IOException {//Stream<Triple> tripleStream, QueryExecutionFactory qef) {
//      linkRsb.out("http://lsq.aksw.org/vocab#isEntailed-JSAC");


        String fileUrl = "file:///home/raven/Downloads/result.nt";
        Model rawModel = RDFDataMgr.loadModel(fileUrl);
        // Create a new model without identities
        rawModel = FluentQueryExecutionFactory.from(rawModel).create().createQueryExecution("CONSTRUCT { ?s ?p ?o } { ?s ?p ?o . FILTER(NOT EXISTS {?o ?p ?s }) }").execConstruct();


        //QueryExecutionFactory qef = FluentQueryExecutionFactory.fromFileNameOrUrl(fileUrl).create();

        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(rawModel).create();
        SparqlFlowEngine engine = new SparqlFlowEngine(qef);



        QueryExecutionFactory dataQef = FluentQueryExecutionFactory.http("http://localhost:8950/swdfsparql").create();
        //QueryExecutionFactory dataQef = FluentQueryExecutionFactory.http("http://localhost:8950/sparql").create();

        ResourceShapeBuilder dataRsb = new ResourceShapeBuilder();
        dataRsb.out(LSQ.text);
        ResourceShape dataShape = dataRsb.getResourceShape();
        LookupService<Node, Resource> dataLs = MapServiceResourceShape.createLookupService(dataQef, dataShape)
                .mapValues(ResourceUtils::asResource);


        ListPaginator<List<Triple>> paginator = engine
            .fromConstruct("CONSTRUCT WHERE { ?s ?p ?o }")
            .batch(50);

        logger.info("Processing " + paginator.fetchCount(null, null) + " batches");

        OutputStream out = new FileOutputStream("/tmp/qc.nt");

        Function<String, Query> parser = SparqlQueryParserImpl.create();

        Range<Long> range = Range.atMost(1l);
        range = Range.all();
        paginator.apply(range).map(triples -> {
                // Create a graph from each batch of triples
                Graph graph = GraphFactory.createDefaultGraph();
                GraphUtil.add(graph, triples);
                return graph;
            }).map(graph -> {
                // For all nodes in the graph, fetch all associated information according to the shape
                // FIXME: We should exclude predicate nodes
                Map<Node, Resource> map = dataLs.apply(() -> GraphUtils.allNodes(graph));

                // Extract the value of LSQ.text and parse it as a query
                Map<Node, Query> m = map.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey,
                        e -> {
                            Query r = parser.apply(e.getValue().getProperty(LSQ.text).getString());
                            r.getPrefixMapping().clearNsPrefixMap();
                            return r;
                        }));

                // Now determine for each triple, whether the normalized queries are
                // equal or isomorphic

                Graph r = GraphFactory.createDefaultGraph();
                graph.find(Node.ANY, Node.ANY, Node.ANY).toSet().stream().filter(t -> {
                    Query a = m.get(t.getSubject());
                    Query b = m.get(t.getObject());

                    if(a == null) {
                        logger.warn("No query for subject " + t.getSubject());
                    }

                    if(b == null) {
                        logger.warn("No query for object " + t.getObject());
                    }


                    boolean s = !Objects.equals(a, b) && a != null && b != null;
                    //System.out.println("r = " + r);
                    return s;

                    //boolean r = jsaSolver.entailed(Objects.toString(a), Objects.toString(b));
                    //return r;
                }).forEach(r::add);


                return r;
                //System.out.println("got batch: " + m);
            }).forEach(g -> {
                Model m = ModelFactory.createModelForGraph(g);
                RDFDataMgr.write(out, m, RDFFormat.NTRIPLES);
            });

        out.flush();
        out.close();

        //LookupService<Node, Resource> nodeToQuery =

        //ls.apply(t);

        //ls.fe


        //LookupServiceUtils.createLookupService(dataQef, dataShape);


        //feed(ms.streamData(null, null))
//
//        SparqlQueryParser parser = SparqlQueryParserImpl.create();
//        //LookupServiceListService.create(listService)
//        ms.streamData(null, null).forEach(r -> {
//            Set<Node> nodes = new HashSet<>();
//            nodes.add(r.asNode());
//            Set<Node> targets = r.listProperties().mapWith(Statement::getObject).mapWith(RDFNode::asNode).toSet();
//            nodes.addAll(targets);
//
//            Map<Node, Query> map = ls.apply(nodes).entrySet().stream().collect(
//                    Collectors.toMap(e -> e.getKey(), e -> parser.apply(e.getValue().getProperty(LSQ.text).getString())));
//
//
//
//
//
//            System.out.println("Resource: " + map);
//            //RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_BLOCKS);
//        });



        //shape.


        //ParameterizedSparqlString pss = new ParameterizedSparqlString();

        //GraphUtils.allNodes(graph)

        //LookupServiceSparqlQuery
    //	qef.createQueryExecution("CONSTRUCT { ?s lsq:asText ?o } WHERE { ?s lsq:asText)
    //	tripleStream.forEach(
    }


    public static void run(String solverShortLabel, SimpleContainmentSolver solver, Supplier<Stream<Resource>> queryIterable) throws Exception {


        //String solverShortLabel = "JSAC";

        String ns = "http://lsq.aksw.org/vocab#";
        Property _isEntailed = ResourceFactory.createProperty(ns + "isEntailed-" + solverShortLabel);
        Property _isNotEntailed = ResourceFactory.createProperty(ns + "isNotEntailed-" + solverShortLabel);
        Property _entailmentError = ResourceFactory.createProperty(ns + "entailmentError-" + solverShortLabel);
        Property _inputError = ResourceFactory.createProperty(ns + "inputError-" + solverShortLabel);

        Function<String, Query> parser = SparqlQueryParserImpl.create();

        Stream<Model> x = queryIterable.get()//.peek((foo) -> System.out.println("foo: " + foo))
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
                            Query aq = parser.apply(aStr);
                            Query bq = parser.apply(bStr);

                            aq.getPrefixMapping().clearNsPrefixMap();
                            bq.getPrefixMapping().clearNsPrefixMap();

                            aq.setLimit(Query.NOLIMIT);
                            bq.setLimit(Query.NOLIMIT);
                            
                            String aEffective = "" + aq;
                            String bEffective = "" + bq;
                            
                            boolean queriesAreTheSame = Objects.equals(aq, bq) && aq != null;
                            if(!queriesAreTheSame) {
                            //System.out.println("r = " + r);
                                boolean isEntailed = solver.entailed(aEffective, bEffective);
                                if (isEntailed) {
                                    m.add(a, _isEntailed, b);
                                } else {
                                    m.add(a, _isNotEntailed, b);
                                }
                            }
                        } catch (Exception ex) {
                            m.add(a, _entailmentError, b);
                            logger.warn("Entailment error", ex);
                        }
                    } catch (Exception ey) {
                        m.add(a, _inputError, b);
                        logger.warn("Input error", ey);
                    }
                    return m;//.listStatements().next();
                })

        );

        // x.forEach(System.out::println);

        // System.out.println(x.count());

        //OutputStream out = new FileOutputStream(new File("/mnt/Data/tmp/swdf-containment-" + solverShortLabel + ".nt"));
        x.forEach(m -> {
            //Model m = ModelFactory.createDefaultModel();
            //m.add(stmt);
            RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES_UTF8);
        });

        //out.flush();
        //out.close();


    }

    // public boo testContain(Resource a, Resource b)
}
