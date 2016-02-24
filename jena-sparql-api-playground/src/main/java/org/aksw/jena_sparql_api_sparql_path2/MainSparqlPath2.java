package org.aksw.jena_sparql_api_sparql_path2;

import java.io.ByteArrayInputStream;

import org.aksw.jena_sparql_api.core.GraphSparqlService;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import scala.Tuple2;


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

    public static void rxJavaTest() {
        //Observable.from(args).subscribe()
        Observable<Object> obs = Observable.create(subscriber -> {
            for(int i = 0; i < 50; ++i) {
                if(!subscriber.isUnsubscribed()) {
                    subscriber.onNext("yay" + i);
                }
            }
            if(!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
        obs.subscribe(x -> System.out.println(x));

        if(true) {
            return;
        }
    }


    //public static FrontierRDD advanceFrontier(FrontierRDD, NFA;, )


    public static void main(String[] args) throws InterruptedException {


        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths());

        //SparqlService coreSparqlService = FluentSparqlService.http("http://fp7-pp.publicdata.eu/sparql", "http://fp7-pp.publicdata.eu/").create();
        //SparqlService coreSparqlService = FluentSparqlService.http("http://localhost:8890/sparql", "http://fp7-pp.publicdata.eu/").create();
        //FluentSparqlServiceFactoryFn.start().configService().

        //SparqlService coreSparqlService = FluentSparqlService.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();

        if (args.length < 1) {
            logger.error("=> wrong parameters number");
            System.err.println("Usage: FileName <path-to-files> <output-path>");
            System.exit(1);
        }

        String fileName = args[0];
        String sparkMasterHost = args.length >= 2 ? args[1] : "local[2]";

        SparkConf sparkConf = new SparkConf()
                .setAppName("BDE-readRDF")
                .setMaster(sparkMasterHost);
                //.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        // TODO Replace with the regex automaton
        Broadcast<int[]> broadcastVar = sparkContext.broadcast(new int[] {1, 2, 3});

//new SimpleEntry<>(1, "test");

        // Map each subject to corresponding predicate/object pairs
        JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd = sparkContext
                .textFile(fileName, 5)
                .filter(line -> !line.trim().isEmpty() & !line.startsWith("#"))
                .map(line -> RDFDataMgr.createIteratorTriples(new ByteArrayInputStream(line.getBytes()), Lang.NTRIPLES, "http://example/base").next())
                .mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
                    private static final long serialVersionUID = -4757627441301230743L;
                    @Override
                    public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
                            throws Exception {
                        return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
                    }
                });

        JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = fwdRdd.mapToPair(new PairFunction<Tuple2<Node, Tuple2<Node, Node>>, Node, Tuple2<Node, Node>>() {
            private static final long serialVersionUID = -1567531441301230743L;
            @Override
            public Tuple2<Node, Tuple2<Node, Node>> call(
                    Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
                return new Tuple2<>(t._2._2, new Tuple2<>(t._2._1, t._1));
            }
        });






//        JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = tripleRdd.mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
//            private static final long serialVersionUID = -4757627441301230743L;
//            @Override
//            public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
//                    throws Exception {
//                return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
//            }
//        });



        fwdRdd.foreach(new VoidFunction<Tuple2<Node, Tuple2<Node, Node>>>() {
            private static final long serialVersionUID = -2954655113824728223L;
            @Override
            public void call(Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
                System.out.println("GOT: " + t);
            }
        });




        //tripleRdd.
        //JavaPairRDD<String, String> x;
        //x.cogroup(other)
//        JavaRDD


        //tripleRdd.saveAsObjectFile("my-triple-rdd.dat");

        sparkContext.close();

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
                            .withPagination(1000)
                        .end()
                    .end()
                .end()
                .create();

        Server server = ServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7533);
        server.join();


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
