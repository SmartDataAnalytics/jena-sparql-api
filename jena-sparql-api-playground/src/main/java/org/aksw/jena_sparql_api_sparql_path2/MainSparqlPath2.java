package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.GraphSparqlService;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggList;
import org.aksw.jena_sparql_api.mapper.AggLiteral;
import org.aksw.jena_sparql_api.mapper.AggMap;
import org.aksw.jena_sparql_api.mapper.AggTransform;
import org.aksw.jena_sparql_api.mapper.BindingMapperProjectVar;
import org.aksw.jena_sparql_api.mapper.MappedQuery;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MainSparqlPath2 {

    private static final Logger logger = LoggerFactory.getLogger(MainSparqlPath2.class);

    public static SparqlService wrapSparqlService(SparqlService coreSparqlService, SparqlStmtParserImpl sparqlStmtParser, Prologue prologue) {

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


    //ListService<Concept, Node, List<Node>>
    /**
     * Maps nodes to their predicates and their count of distinct values
     *
     * @param qef
     * @param reverse
     * @return
     */
    public static LookupService<Node, Map<Node, Number>> createListServicePredicates(QueryExecutionFactory qef, boolean reverse) {
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

        ListService<Concept, Node, Map<Node, Number>> lsx = ListServiceUtils.createListServiceMappedQuery(qef, mappedQuery, false);
        LookupService<Node, Map<Node, Number>> result = LookupServiceListService.create(lsx);


        return result;
    }


    public static void main(String[] args) throws InterruptedException {


        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());

        String pathExprStr = createPathExprStr("http://dbpedia.org/ontology/president");
        Node s = NodeFactory.createURI("http://dbpedia.org/resource/James_K._Polk");

        String queryStr = "SELECT ?path { <" + s + "> jsafn:kShortestPaths ('" + pathExprStr + "' ?path <http://dbpedia.org/resource/Felix_Grundy> 471199) }";


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

        SparqlStmtParserImpl sparqlStmtParser = SparqlStmtParserImpl.create(SparqlParserConfig.create(Syntax.syntaxARQ, prologue));


        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription, Object authenticator) {

                SparqlService coreSparqlService = FluentSparqlService.http(serviceUri, datasetDescription, (HttpAuthenticator)authenticator).create();
                SparqlService r = wrapSparqlService(coreSparqlService, sparqlStmtParser, prologue);
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
                    DatasetDescription datasetDescription, Object authenticator) {

                SparqlService r = FluentSparqlService.http(serviceUri, datasetDescription, (HttpAuthenticator)authenticator).create();
                //SparqlService r = wrapSparqlService(coreSparqlService, sparqlStmtParser, prologue);
                return r;
            }
        };
        ssf2 = FluentSparqlServiceFactory.from(ssf2)
                .configFactory()
                    .defaultServiceUri("http://localhost:8890/sparql")
                .end()
                .create();



        SparqlService ssps = ssf2.createSparqlService(null, DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt/summary/predicate/"), null);
        SparqlService sspjs = ssf2.createSparqlService(null, DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt/summary/predicate-join/"), null);

//        System.out.println("Loading predicate summary");
//        Map<Node, Long> ps = EdgeReducer.loadPredicateSummary(ssps.getQueryExecutionFactory());
//        System.out.println("Predicate summary is: " + ps.size());
//
//        System.out.println("Loading join summary");
//        BiHashMultimap<Node, Node> pjs = EdgeReducer.loadJoinSummary(sspjs.getQueryExecutionFactory());
//        System.out.println("Done: join summary is " + pjs.size());


        if(true) {
            //ssf.createSparqlService("http://, datasetDescription, authenticator)
            SparqlService ss = ssf.createSparqlService(null, DatasetDescriptionUtils.createDefaultGraph("http://2016.eswc-conferences.org/top-k-shortest-path-large-typed-rdf-graphs-challenge/training_dataset.nt"), null);
            QueryExecutionFactory qef = ss.getQueryExecutionFactory();
            //ListService<Concept, Node, List<Node>> lsx =
            //LookupService<Node, List<Node>> ls = LookupServiceListService.create(lsx);
            LookupService<Node, Map<Node, Number>> fwdLs = createListServicePredicates(qef, false);
            LookupService<Node, Map<Node, Number>> bwdLs = createListServicePredicates(qef, true);

            // Fetch the properties for the source and and states
            Map<Node, Map<Node, Number>> fwdPreds = fwdLs.apply(Collections.singleton(s));
            Map<Node, Map<Node, Number>> bwdPreds = bwdLs.apply(Collections.singleton(s));

            System.out.println(fwdPreds);
            System.out.println(bwdPreds);

//            Map<Node, Number> fwdNodes = new HashSet<>(fwdPreds.get(s));
//            Map<Node, Number> bwdNodes = new HashSet<>(bwdPreds.get(s));
//
//            PredicateClass pc = new PredicateClass(
//                    new ValueSet<Node>(true, fwdNodes),
//                    new ValueSet<Node>(true, bwdNodes));





            QueryExecution qe = qef.createQueryExecution(queryStr);
            ResultSet rs = qe.execSelect();
            ResultSetFormatter.outputAsJSON(System.out, rs);


        } else {
            Server server = ServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7533);
            server.join();
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
