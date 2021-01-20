package org.aksw.jena_sparql_api_sparql_path2.main;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.jena.jgrapht.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.GraphSparqlService;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceCacheMem;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceUtils;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggLiteral;
import org.aksw.jena_sparql_api.mapper.AggMap;
import org.aksw.jena_sparql_api.mapper.AggTransform;
import org.aksw.jena_sparql_api.mapper.BindingMapperProjectVar;
import org.aksw.jena_sparql_api.mapper.MappedQuery;
//import org.aksw.jena_sparql_api.server.utils.SparqlServerUtils;
import org.aksw.jena_sparql_api.sparql_path2.EdgeFactoryLabeledEdge;
import org.aksw.jena_sparql_api.sparql_path2.JGraphTUtils;
import org.aksw.jena_sparql_api.sparql_path2.JoinSummaryUtils;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.NfaImpl;
import org.aksw.jena_sparql_api.sparql_path2.PathCompiler;
import org.aksw.jena_sparql_api.sparql_path2.PathExecutionUtils;
import org.aksw.jena_sparql_api.sparql_path2.PredicateClass;
import org.aksw.jena_sparql_api.sparql_path2.PropertyFunctionFactoryKShortestPaths;
import org.aksw.jena_sparql_api.sparql_path2.PropertyFunctionKShortestPaths;
import org.aksw.jena_sparql_api.sparql_path2.ValueSet;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletPath;
import org.aksw.jena_sparql_api_sparql_path2.playground.EdgeReducer;
import org.aksw.jena_sparql_api_sparql_path2.playground.JoinSummaryService;
import org.aksw.jena_sparql_api_sparql_path2.playground.JoinSummaryService2;
import org.aksw.jena_sparql_api_sparql_path2.playground.JoinSummaryService2Impl;
import org.aksw.jena_sparql_api_sparql_path2.playground.JoinSummaryServiceImpl;
import org.aksw.jena_sparql_api_sparql_path2.playground.NfaAnalysisResult;
import org.aksw.jena_sparql_api_sparql_path2.playground.YensKShortestPaths;
import org.apache.http.client.HttpClient;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;



public class MainSparqlPath2 {

    private static final Logger logger = LoggerFactory.getLogger(MainSparqlPath2.class);

    public static SparqlService proxySparqlService(SparqlService coreSparqlService, SparqlStmtParserImpl sparqlStmtParser, Prologue prologue) {

        GraphSparqlService graph = new GraphSparqlService(coreSparqlService);
        Model model = ModelFactory.createModelForGraph(graph);

        Context context = ARQ.getContext().copy();

        SparqlService result = FluentSparqlService
                .from(model, context)
                .config()
                    .configQuery()
                        .withParser(sparqlStmtParser.getQueryParser())
                        .withPrefixes(prologue.getPrefixMapping(), true) // If a query object is without prefixes, inject them
                    .end()
                .end()
                .create();


        context.put(PropertyFunctionKShortestPaths.PROLOGUE, prologue);
        context.put(PropertyFunctionKShortestPaths.SPARQL_SERVICE, coreSparqlService);

        return result;
    }

    public static String createPathExprStr(String predicate) {
        String result = "(<" + predicate + ">/(!<http://foo>)*)|(!<http://foo>)*/<" + predicate + ">";
        return result;
    }


    public static LookupService<Node, Map<Node, Number>> createJoinSummaryLookupService(SparqlQueryConnection qef, boolean reverse) {

        Query query = new Query();
        QueryFactory.parse(query, "PREFIX o: <http://example.org/ontology/> SELECT ?x ?y ((<http://www.w3.org/2001/XMLSchema#double>(?fy) / <http://www.w3.org/2001/XMLSchema#double>(?fx)) As ?z) { ?s o:sourcePredicate ?x ; o:targetPredicate ?y ; o:freqSource ?fx ; o:freqTarget ?fy }", "http://example.org/base/", Syntax.syntaxARQ);

        Var source = !reverse ? Vars.x : Vars.y;
        Var target = !reverse ? Vars.y : Vars.x;
        Var freq = Vars.z;



        Agg<Map<Node, Number>> agg = AggMap.create(
                BindingMapperProjectVar.create(target),
                AggTransform.create(AggLiteral.create(BindingMapperProjectVar.create(freq)), (node) -> {
                    Number result;
                    // TODO Make a bug report that sometimes double rdf terms in json serialization in virtuoso 7.2.2 turn up as NAN
                    try {
                        Number n = (Number)node.getLiteralValue();
                        result = reverse ? 1.0 / n.doubleValue() : n;
                    } catch(Exception e) {
                        logger.warn("Not a numeric literal: " + node);
                        result = 1.0;
                    }
                    return result;
                }));
        MappedQuery<Map<Node, Number>> mappedQuery = MappedQuery.create(query, source, agg);

        MapService<Concept, Node, Map<Node, Number>> lsx = MapServiceUtils.createListServiceMappedQuery(qef, mappedQuery, false);
        LookupService<Node, Map<Node, Number>> result = LookupServiceListService.create(lsx);

        result = LookupServicePartition.create(result, 100, 4);
        result = LookupServiceCacheMem.create(result, 20000);

        return result;
    }

    public static JoinSummaryService createJoinSummaryService(SparqlQueryConnection qef) {
        JoinSummaryServiceImpl result = new JoinSummaryServiceImpl(
                createJoinSummaryLookupService(qef, false),
                createJoinSummaryLookupService(qef, true));

        return result;
    }


    public static <S> Nfa<S, LabeledEdge<S, PredicateClass>> reverseNfa(Nfa<S, LabeledEdge<S, PredicateClass>> nfa) {
        EdgeFactoryLabeledEdge<S, PredicateClass> edgeFactory = new EdgeFactoryLabeledEdge<S, PredicateClass>();
        Graph<S, LabeledEdge<S, PredicateClass>> bwdGraph = new DefaultDirectedGraph<S, LabeledEdge<S, PredicateClass>>(
        		null, () -> edgeFactory.createEdge(null, null), false);


        //DirectedGraph<S, LabeledEdge<S, PredicateClass>> bwdGraph = new SimpleDirectedGraph<>(new LabeledEdgeFactoryImpl<S, PredicateClass>());

        Graph<S, LabeledEdge<S, PredicateClass>> fwdGraph = nfa.getGraph();

        fwdGraph.vertexSet().stream().forEach(v -> bwdGraph.addVertex(v));
        fwdGraph.edgeSet().stream().forEach(
                e -> bwdGraph.addEdge(fwdGraph.getEdgeTarget(e), fwdGraph.getEdgeSource(e), new LabeledEdgeImpl<>(
                        fwdGraph.getEdgeTarget(e),
                        fwdGraph.getEdgeSource(e),
                        e.getLabel() == null ? null : PredicateClass.reverse(e.getLabel()))));

        Nfa<S, LabeledEdge<S, PredicateClass>> result = new NfaImpl<>(bwdGraph, nfa.getEndStates(), nfa.getStartStates());
        return result;
    }


    //ListService<Concept, Node, List<Node>>
    /**
     * Maps nodes to their predicates and their count of distinct values
     *
     * @param qef
     * @param reverse
     * @return
     */
    public static LookupService<Node, Map<Node, Number>> createListServicePredicates(SparqlQueryConnection qef, boolean reverse) {
        Query query = new Query();
        query.setQuerySelectType();
        query.setDistinct(true);
        query.getProject().add(Vars.s);
        query.getProject().add(Vars.p);
        query.getProject().add(Vars.x, new ExprAggregator(Vars.y, new AggCountVarDistinct(new ExprVar(Vars.o))));
        query.getGroupBy().add(Vars.s);
        query.getGroupBy().add(Vars.p);
        Triple t = new Triple(Vars.s, Vars.p, Vars.o);
        if(reverse) {
            t = TripleUtils.swap(t);
        }

        query.setQueryPattern(
                ElementUtils.createElement(t));


        Agg<Map<Node, Number>> agg = AggMap.create(
                BindingMapperProjectVar.create(Vars.p),
                AggTransform.create(AggLiteral.create(BindingMapperProjectVar.create(Vars.x)), (node) -> (Number)node.getLiteralValue()));
        MappedQuery<Map<Node, Number>> mappedQuery = MappedQuery.create(query, Vars.s, agg);

        MapService<Concept, Node, Map<Node, Number>> lsx = MapServiceUtils.createListServiceMappedQuery(qef, mappedQuery, false);
        LookupService<Node, Map<Node, Number>> result = LookupServiceListService.create(lsx);


        return result;
    }


    public static void printNfa(Nfa<?, ?> nfa) {
        System.out.println("NFA: " + nfa);
        System.out.println(nfa.getStartStates());
        System.out.println(nfa.getEndStates());
        nfa.getGraph().edgeSet().forEach(x -> System.out.println(x));

    }

    public static <V, E> void makeConsolidated(Graph<V, E> graph, V superVertex, Set<V> vertices, boolean reverse) {
        graph.addVertex(superVertex);
        if(!reverse) {
            vertices.stream().forEach(v -> graph.addEdge(superVertex, v));
        } else {
            vertices.stream().forEach(v -> graph.addEdge(v, superVertex));
        }
    }

    public static <V, E> void makeConsolidated(Graph<V, E> graph, Set<V> vertices, Supplier<V> vertexFactory, boolean reverse) {
        if(vertices.size() > 1) {
            V newVertex = vertexFactory.get();
            makeConsolidated(graph, newVertex, vertices, reverse);
        }
    }

    /**
     * If the nfa has multiple start and end nodes, add a sinle super start/end nodes
     *
     * https://en.wikipedia.org/wiki/Maximum_flow_problem#Multi-source_multi-sink_maximum_flow_problem
     */
    public static <S, T> void makeConsolidated(Nfa<S, T> nfa, Supplier<S> vertexFactory) {
        Graph<S, T> graph = nfa.getGraph();

        makeConsolidated(graph, nfa.getStartStates(), vertexFactory, false);
        makeConsolidated(graph, nfa.getEndStates(), vertexFactory, true);
    }



    /**
     * Label the edges
     *
     * @param nfa
     */
    public static <S, T> void labelWithPredicateCosts(Nfa<S, T> nfa) {

    }


//    public static NestedPath<V, E> removeEpsEdges(NestedPath<V, E>) {
//
//    }

    public static Graph<Node, DefaultEdge> createJoinSummaryGraph(QueryExecutionFactory qef) { //Model model) {
        //QueryExecutionFactory qef = FluentQueryExecutionFactory.model(model).create();

        String queryStr = "PREFIX o: <http://example.org/ontology/> \n"
                        + "SELECT ?x ?y { \n"
                        + " ?s a o:PredicateJoinSummary ;\n"
                        + "    o:sourcePredicate ?x ;\n"
                        + "    o:targetPredicate ?y \n"
                        + "}\n"
                        ;
        System.out.println(queryStr);
        ResultSet rs = qef.createQueryExecution(queryStr).execSelect();

        Graph<Node, DefaultEdge> result = new DefaultDirectedGraph<>(DefaultEdge.class);
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node x = binding.get(Vars.x);
            Node y = binding.get(Vars.y);
            result.addVertex(x);
            result.addVertex(y);
            result.addEdge(x, y);
        }
        return result;
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths(ss -> null));

        String queryStr;

        DatasetDescription dataset;
        DatasetDescription predDataset;
        DatasetDescription predJoinDataset;
        String pathExprStr;
        Node startNode;
        Node endNode;
        Model joinSummaryModel;
        Node desiredPred;

        Graph<Node, DefaultEdge> fullJoinSummaryBaseGraph;

        if(true) {
            Stopwatch sw = Stopwatch.createStarted();

            //joinSummaryModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/Spark-RDF/tmp/fp7-summary-predicate-join.nt");
            //System.out.println("Join Summary Read took: " + sw.stop().elapsed(TimeUnit.SECONDS) + " for " + joinSummaryModel.size() + " triples");

            Model model = ModelFactory.createDefaultModel();
            //RDFDataMgr.read(model, "classpath://dataset-fp7.ttl");
            RDFDataMgr.read(model, "dataset-fp7.ttl", Lang.TTL);

            Resource ds = ResourceFactory.createResource("http://example.org/resource/data-fp7");

            //String q = "Select ?service ?graph"


            dataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/");
            predDataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/summary/predicate/");
            predJoinDataset = DatasetDescriptionUtils.createDefaultGraph("http://fp7-pp.publicdata.eu/summary/predicate-join/");

            desiredPred = NodeFactory.createURI("http://fp7-pp.publicdata.eu/ontology/funding");
            pathExprStr = createPathExprStr("http://fp7-pp.publicdata.eu/ontology/funding");
            //pathExprStr = "<http://fp7-pp.publicdata.eu/ontology/funding>/^<http://foo>/<http://fp7-pp.publicdata.eu/ontology/funding>/<http://fp7-pp.publicdata.eu/ontology/partner>/!<http://foobar>*";
            startNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/257943");
            endNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/city/France-PARIS");

            queryStr = "PREFIX jsafn: <http://jsa.aksw.org/fn/> SELECT ?path { <" + startNode.getURI() + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <" + endNode.getURI() + "> 471199) }";

        } else {
            Stopwatch sw = Stopwatch.createStarted();

            //joinSummaryModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/Spark-RDF/tmp/eswc-summary-predicate-join.nt");
            //System.out.println("Join Summary Read took: " + sw.stop().elapsed(TimeUnit.SECONDS) + " for " + joinSummaryModel.size() + " triples");

            dataset = DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt");
            predDataset = DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt/summary/predicate/");
            predJoinDataset = DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt/summary/predicate-join/");

            desiredPred = NodeFactory.createURI("http://dbpedia.org/ontology/president");
            pathExprStr = createPathExprStr("http://dbpedia.org/ontology/president");
            startNode = NodeFactory.createURI("http://dbpedia.org/resource/James_K._Polk");
            endNode = NodeFactory.createURI("http://dbpedia.org/resource/Felix_Grundy");
            queryStr = "SELECT ?path { <" + startNode.getURI() + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <" + endNode.getURI() + "> 471199) }";
        }

        System.out.println("Query string: " + queryStr);

        //SparqlService coreSparqlService = FluentSparqlService.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();
        //SparqlService coreSparqlService = FluentSparqlService.http("http://localhost:8890/sparql", "http://fp7-pp.publicdata.eu/").create();
        //FluentSparqlServiceFactoryFn.start().configService().

        //SparqlService coreSparqlService = FluentSparqlService.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();




//        tripleRdd.mapToPair(new PairFunction<Triple, K2, V2>() {
//            @Override
//            public Tuple2<K2, V2> call(Triple t) throws Exception {
//
//            }
//        });


//              val rs = text.map(NTriplesParser.parseTriple)
//
//              val indexedmap = (rs.map(_._1) union rs.map(_._3)).distinct.zipWithIndex //indexing
//              val vertices: RDD[(VertexId, String)] = indexedmap.map(x => (x._2, x._1))
//              val _iriToId: RDD[(String, VertexId)] = indexedmap.map(x => (x._1, x._2))
//
//              val tuples = rs.keyBy(_._1).join(indexedmap).map({
//                case (k, ((s, p, o), si)) => (o, (si, p))
//              })
//
//              val edges: RDD[Edge[String]] = tuples.join(indexedmap).map({
//                case (k, ((si, p), oi)) => Edge(si, oi, p)
//              })

              // TODO is there a specific reason to not return the graph directly? ~ Claus
              //_graph =
//              Graph(vertices, edges)
//
//              new {
//                val graph = Graph(vertices, edges)
//                val iriToId = _iriToId
//              }



        PrefixMappingImpl pm = new PrefixMappingImpl();
        pm.setNsPrefix("jsafn", "http://jsa.aksw.org/fn/");
        pm.setNsPrefixes(PrefixMapping.Extended);
        Prologue prologue = new Prologue(pm);

        SparqlStmtParserImpl sparqlStmtParser = SparqlStmtParserImpl.create(new SparqlParserConfig(Syntax.syntaxARQ, prologue));


        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription, HttpClient httpClient) {

                SparqlService coreSparqlService = FluentSparqlService.http(serviceUri, datasetDescription, httpClient).create();
                SparqlService r = proxySparqlService(coreSparqlService, sparqlStmtParser, prologue);
                return r;
            }
        };

        ssf = FluentSparqlServiceFactory.from(ssf)
                .configFactory()
                    //.defaultServiceUri("http://dbpedia.org/sparql")
                    .defaultServiceUri("http://localhost:8890/sparql")
                    .configService()
                        .configQuery()
                            //.withPagination(1000)
                        .end()
                    .end()
                .end()
                .create();




        SparqlServiceFactory ssf2 = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription, HttpClient httpClient) {

                SparqlService r = FluentSparqlService.http(serviceUri, datasetDescription, httpClient).create();
                //SparqlService r = wrapSparqlService(coreSparqlService, sparqlStmtParser, prologue);
                return r;
            }
        };
        ssf2 = FluentSparqlServiceFactory.from(ssf2)
                .configFactory()
                    .defaultServiceUri("http://localhost:8890/sparql")
                    .configService()
                        .configQuery()
                            .withPagination(1000)
                        .end()
                    .end()
                .end()
                .create();


        SparqlService ssps = ssf2.createSparqlService(null, predDataset, null);
        SparqlService sspjs = ssf2.createSparqlService(null, predJoinDataset, null);

//        System.out.println("Loading predicate summary");
//        Map<Node, Long> ps = EdgeReducer.loadPredicateSummary(ssps.getQueryExecutionFactory());
//        System.out.println("Predicate summary is: " + ps.size());
//
//        System.out.println("Loading join summary");
//        BiHashMultimap<Node, Node> pjs = EdgeReducer.loadJoinSummary(sspjs.getQueryExecutionFactory());
//        System.out.println("Done: join summary is " + pjs.size());


        if(true) {
            //ssf.createSparqlService("http://, datasetDescription, authenticator)
            SparqlService ss = ssf.createSparqlService(null, dataset, null);

            QueryExecutionFactory tmp = ss.getQueryExecutionFactory();
            SparqlQueryConnection qef = new SparqlQueryConnectionJsa(tmp);
            //ListService<Concept, Node, List<Node>> lsx =
            //LookupService<Node, List<Node>> ls = LookupServiceListService.create(lsx);
            LookupService<Node, Map<Node, Number>> fwdLs = createListServicePredicates(qef, false);
            LookupService<Node, Map<Node, Number>> bwdLs = createListServicePredicates(qef, true);

            // Fetch the properties for the source and end states
            Map<Node, Map<Node, Number>> fwdPreds = fwdLs.fetchMap(Arrays.asList(startNode, endNode));
            Map<Node, Map<Node, Number>> bwdPreds = bwdLs.fetchMap(Arrays.asList(startNode, endNode));

            Pair<Map<Node, Number>> startPredFreqs =
                    new Pair<>(fwdPreds.getOrDefault(startNode, Collections.emptyMap()), bwdPreds.getOrDefault(startNode, Collections.emptyMap()));

            Pair<Map<Node, Number>> endPredFreqs =
                    new Pair<>(fwdPreds.getOrDefault(endNode, Collections.emptyMap()), bwdPreds.getOrDefault(endNode, Collections.emptyMap()));

            //Pair<Set<Node>> startPreds = new Pair<>(startPredFreqs.get(0).keySet(), startPredFreqs.get(1).keySet());
            //Pair<Set<Node>> endPreds = new Pair<>(endPredFreqs.get(0).keySet(), endPredFreqs.get(1).keySet());


            System.out.println(fwdPreds);
            System.out.println(bwdPreds);

            Path path = PathParser.parse(pathExprStr, PrefixMapping.Extended);
            Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);
            System.out.println("FORWARD NFA for " + path);
            printNfa(nfa);


            QueryExecutionFactory rawQef = ssf2.createSparqlService(null, dataset, null).getQueryExecutionFactory();

            Function<Pair<ValueSet<Node>>, Function<Iterable<Node>, Map<Node, Set<Triplet<Node, Node>>>>> createTripletLookupService =
                    pc -> f -> PathExecutionUtils.createLookupService(new SparqlQueryConnectionJsa(rawQef), pc).fetchMap(f);

            Set<Entry<Integer, Node>> starts = new HashSet<>();
            nfa.getStartStates().forEach(s -> starts.add(new SimpleEntry<>(s, startNode)));

//            Multimap<Entry<Integer, Node>, Triplet<Entry<Integer, Node>, Directed<Node>>> succs = NfaDijkstra.getSuccessors(
//                    nfa,
//                    LabeledEdgeImpl::isEpsilon,
//                    e -> e.getLabel(),
//                    createTripletLookupService,
//                    starts);
//            System.out.println("Successors: " + succs);

//            TripletPath<Node, Node> shortestPath = NfaDijkstra.dijkstra(
//                    nfa,
//                    LabeledEdgeImpl::isEpsilon,
//                    e -> e.getLabel(),
//                    createTripletLookupService,
//                    startNode,
//                    endNode);

//            successors, source, target, maxK);
            List<TripletPath<Entry<Integer, Node>, Directed<Node>>> kPaths =
                    YensKShortestPaths.findPaths(
                          nfa,
                          x -> x.getLabel() == null, //LabeledEdgeImpl::isEpsilon,
                          e -> e.getLabel(),
                          createTripletLookupService,
                          startNode,
                          endNode,
                          10);

            kPaths.forEach(x -> System.out.println("kPaths: " + x));


            //MinSourceSinkCut<Integer, LabeledEdge<Integer, PredicateClass>> x = new MinSourceSinkCut<Integer, LabeledEdge<Integer, PredicateClass>>(nfa.getGraph());
            //x.computeMinCut(source, sink);

            //nfa.getGraph().getE

            //Set<?> cut = x.getCutEdges();
            //System.out.println("CUT: " + cut);

            JoinSummaryService joinSummaryService = createJoinSummaryService(new SparqlQueryConnectionJsa(sspjs.getQueryExecutionFactory()));

            fullJoinSummaryBaseGraph = createJoinSummaryGraph(sspjs.getQueryExecutionFactory());//joinSummaryModel);


            JoinSummaryService2 jss2 = new JoinSummaryService2Impl(sspjs.getQueryExecutionFactory());
//            Map<Node, Number> test = jss2.fetchPredicates(Arrays.<Node>asList(NodeFactory.createURI("http://dbpedia.org/property/owner")), false);
//            Map<Node, Number> test = jss2.fetchPredicates(Arrays.<Node>asList(NodeFactory.createURI("http://dbpedia.org/property/novPrecipInch")), true);

//            System.out.println("join summary 2: " + test);

//            Node issue = NodeFactory.createURI("http://dbpedia.org/ontology/owner");
//            Map<Node, Map<Node, Number>> test = joinSummaryService.fetch(Collections.singleton(issue), false);
//            System.out.println("Test: " + test);


            System.out.println("PATHS IN THE NFA:");
            //List<NestedPath<Integer, LabeledEdge<Integer, PredicateClass>>>
            List<TripletPath<Integer, LabeledEdge<Integer, PredicateClass>>> nfaPaths = JGraphTUtils.getAllPaths(nfa.getGraph(), nfa.getStartStates().iterator().next(), nfa.getEndStates().iterator().next())
                    .stream().map(p -> p.asSimplePath()).collect(Collectors.toList());


            // for each nfa path, decide its direction
            //


            //nfaPaths.forEach(item -> System.out.println(item.asSimplePath()));

            // TODO Create a sub-nfa for each path, then check for isomorphy
            // http://stackoverflow.com/questions/9448754/is-there-an-efficient-algorithm-to-decide-whether-the-language-accepted-by-one-n

            Map<Object, TripletPath<Integer, LabeledEdge<Integer, PredicateClass>>> map = new HashMap<>();
            for(TripletPath<Integer, LabeledEdge<Integer, PredicateClass>> nfaPath : nfaPaths) {
                List<Triplet<Integer, LabeledEdge<Integer, PredicateClass>>> key = nfaPath.getTriplets().stream()
                    .filter(t -> LabeledEdgeImpl.isEpsilon(t.getPredicate()))
                    .collect(Collectors.toList());

                map.put(key, nfaPath);
            }

            map.entrySet().stream().forEach(entry -> System.out.println("REDUCED: " + entry.getValue()));

            // Cluster the paths by removing the epsilon edges
            //Map<Set<Tripl>>
//            nfaPaths.stream()
//                .map(item -> item.asSimplePath().getTriples())
//                .filter(triplets -> triplets.stream().filter(t -> LabeledEdgeImpl.isEpsilon(t.getPredicate())))
//                .collect(Collectors.toList());



            Stopwatch swFwd = Stopwatch.createStarted();
            NfaAnalysisResult<Integer> fwdCosts = EdgeReducer.<Integer, LabeledEdge<Integer, PredicateClass>>estimateFrontierCost(
                    nfa,
                    LabeledEdgeImpl::isEpsilon,
                    e -> e.getLabel(),
                    startPredFreqs,
                    joinSummaryService);

            System.out.println("Cost estimation took" + swFwd.elapsed(TimeUnit.SECONDS));
//            NfaUtils


            // Given the join graph and the nfa, determine for a given nestedPath in a given set of nfa states of whether it can reach the set of predicates leading to the target states.

//            DirectedGraph<Node, DefaultaEdge> rawJoinGraph = fwdCosts.joinGraph;


            Graph<Node, DefaultEdge> rawJoinGraph = fullJoinSummaryBaseGraph;

            Graph<Node, DefaultEdge> tmpEndAugJoinGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
            Node augStart = NodeFactory.createURI("http://start.org");
            Node augEnd = NodeFactory.createURI("http://end.org");


            JGraphTUtils.addSuperVertex(tmpEndAugJoinGraph, augEnd, endPredFreqs.get(1).keySet(), endPredFreqs.get(0).keySet());

            Graph<Node, DefaultEdge> endAugJoinGraph = new AsGraphUnion<Node, DefaultEdge>(rawJoinGraph, tmpEndAugJoinGraph);

//
//            augJoinGraph.addVertex(augStart);
//            augJoinGraph.addVertex(augEnd);
//
//            for(int i = 0; i < 2; ++i) {
//                boolean reverse = i == 1;
//                startPredFreqs.get(i).keySet().forEach(pred -> {
//                    augJoinGraph.addVertex(pred);
//                    if(!reverse) {
//                        augJoinGraph.addEdge(augStart, pred);
//                    } else {
//                        augJoinGraph.addEdge(pred, augStart);
//                    }
//                });
//                endPredFreqs.get(i).keySet().forEach(pred -> {
//                    augJoinGraph.addVertex(pred);
//                    if(!reverse) {
//                        augJoinGraph.addEdge(augEnd, pred);
//                    } else {
//                        augJoinGraph.addEdge(pred, augEnd);
//                    }
//                });
//            }



//            DirectedGraph<Node, DefaultEdge> joinGraph = new DirectedGraphUnion<Node, DefaultEdge>(rawJoinGraph, augJoinGraph);


//            joinGraph.edgeSet().stream()
//                .map(e -> JGraphTUtils.toTriplet(joinGraph, e))
//                .forEach(xxx -> System.out.println("join: " + xxx));

            // The goal is, for the given start (augStart), to determine, which predicates can actually reach the target
            // We can do that, by only linking augStart to a single predicate (of either direction) and checking whether a path exists

//            List<NestedPath<Node, DefaultEdge>> reachabilityPaths = findJoinSummaryPaths(
                    ///fa, augStart, augEnd, joinGraph);
                    //x-> true);

            Set<Node> reachablePreds = startPredFreqs.getKey().keySet().stream().filter(pred ->
                JoinSummaryUtils.existsReachability(
                        nfa,
                        nfa.getStartStates(),
                        endAugJoinGraph,
                        augEnd,
                        pred,
                        false
                        )).collect(Collectors.toSet());

            System.out.println("Reachability: " + reachablePreds.size() + "/" + startPredFreqs.getKey().keySet().size() + ": "+ reachablePreds + " out of " + startPredFreqs.getKey().keySet());


            System.out.println("------------");

            Nfa<Integer, LabeledEdge<Integer, PredicateClass>> reverseNfa = reverseNfa(nfa);
            System.out.println("BACKWARD NFA");
            printNfa(reverseNfa);


            NfaAnalysisResult<Integer> bwdCosts = EdgeReducer.<Integer, LabeledEdge<Integer, PredicateClass>>estimateFrontierCost(
                    reverseNfa,
                    LabeledEdgeImpl::isEpsilon,
                    e -> e.getLabel(),
                    endPredFreqs,
                    joinSummaryService);


            // 2. Label the nfa by iterating the nfa backwards

            // 3. For every path in the nfa,


//

//            Map<Node, Number> fwdNodes = new HashSet<>(fwdPreds.get(s));
//            Map<Node, Number> bwdNodes = new HashSet<>(bwdPreds.get(s));
//
//            PredicateClass pc = new PredicateClass(
//                    new ValueSet<Node>(true, fwdNodes),
//                    new ValueSet<Node>(true, bwdNodes));



            boolean execQuery = true;
            if(execQuery) {
            	Query query = QueryFactory.create(queryStr);
                QueryExecution qe = qef.query(query);
                ResultSet rs = qe.execSelect();
                ResultSetFormatter.outputAsJSON(System.out, rs);
            }


        } else {
//            Server server = SparqlServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7533);
//            server.join();
        }




        // Create a datasetGraph backed by the SPARQL service to DBpedia
//        DatasetGraphSparqlService datasetGraph = new DatasetGraphSparqlService(coreSparqlService);

        // TODO Add support for sparqlService transformation
//        final SparqlServiceFactory ssf = FluentSparqlServiceFactory.from(new SparqlServiceFactoryHttp())
//            .configFactory()
//                .defaultServiceUri("http://dbpedia.org/sparql")
//                .configService()
//                    .configQuery()
//                        .withParser(sparqlStmtParser.getQueryParser())
//                        .withPrefixes(pm, true) // If a query object is without prefixes, inject them
//                    .end()
//                .end()
//            .end()
//            .create();



//        SparqlServiceFactory ssf = new SparqlServiceFactory() {
//            @Override
//            public SparqlService createSparqlService(String serviceUri,
//                    DatasetDescription datasetDescription,
//                    Object authenticator) {
//                return sparqlService;
//            }
//
//        };



        //Model model = ModelFactory.createDefaultModel();
        //GraphQueryExecutionFactory

        //String queryStr = "SELECT * { ?s ?p ?o } LIMIT 10";
//
        //String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('(rdf:type|!rdf:type)*' ?path <http://fp7-pp.publicdata.eu/resource/city/France-PARIS>) }";
//        String queryStr = "SELECT ?path { <http://fp7-pp.publicdata.eu/resource/project/257943> jsafn:kShortestPaths ('rdf:type*' ?path) }";
//        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
//
//        for(int i = 0; i < 1; ++i) {
//            QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//            QueryExecution qe = qef.createQueryExecution(queryStr);
////            //System.out.println("query: " + qe.getQuery());
//            System.out.println("Result");
//            ResultSet rs = qe.execSelect();
//            System.out.println(ResultSetFormatter.asText(rs));
//            //ResultSetFormatter.outputAsTSV(System.out, rs);
//        }

      //Thread.sleep(1000);
    }


}
