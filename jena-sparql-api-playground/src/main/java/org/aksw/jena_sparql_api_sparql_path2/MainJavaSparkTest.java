package org.aksw.jena_sparql_api_sparql_path2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceUtils;
import org.aksw.jena_sparql_api.stmt.SparqlParserConfig;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.aksw.jena_sparql_api_sparql_path.spark.NfaExecutionSpark;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ResourceLoader;

import com.google.common.base.Stopwatch;

import scala.Tuple2;

public class MainJavaSparkTest {

    private static final Logger logger = LoggerFactory.getLogger(MainJavaSparkTest.class);

    /**
     * Arguments:
     * dcat-service=URL url to a dcat dataset index
     * dataset=string name of the dataset (will be looked up in the index)
     *
     *
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {



        if (args.length < 1) {
            logger.error("=> wrong parameters number");
            System.err.println("Usage: FileName <path-to-files> <output-path>");
            System.exit(1);
        }

        String fileName = args[0];
        String sparkMasterHost = args.length >= 2 ? args[1] : "local[2]";

//        Option<String> tmp = SparkContext.jarOfClass(MainJavaSparkTest.class);
//        String jar = tmp.get();
//        logger.info("Jar: " + jar);

        SparkConf sparkConf = new SparkConf()
                .setAppName("BDE-readRDF")
                .setMaster(sparkMasterHost)
                //.setJars(new String[] { jar })
                //.setJars(new String[] {"http://cstadler.aksw.org/files/spark/jena-sparql-api-playground-3.0.1-2-SNAPSHOT-jar-with-dependencies.jar"})
                //.set("spark.local.ip", "localhost") //"139.18.8.88")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        Stopwatch sw = Stopwatch.createStarted();

        JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd = sparkContext
            .textFile(fileName, 5)
            //.parallelize(triples)
            .filter(line -> !line.trim().isEmpty() & !line.startsWith("#"))
            .map(line -> RDFDataMgr.createIteratorTriples(new ByteArrayInputStream(line.getBytes()), Lang.NTRIPLES, "http://example/base").next())
            .mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
                private static final long serialVersionUID = -4757627441301230743L;
                @Override
                public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
                        throws Exception {
                    return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
                }
            })
            .cache();

        System.out.println("Loaded FWD RDD:" + sw.elapsed(TimeUnit.SECONDS));

        JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = fwdRdd
            .mapToPair(new PairFunction<Tuple2<Node, Tuple2<Node, Node>>, Node, Tuple2<Node, Node>>() {
                private static final long serialVersionUID = -1567531441301230743L;
                @Override
                public Tuple2<Node, Tuple2<Node, Node>> call(
                        Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
                    return new Tuple2<>(t._2._2, new Tuple2<>(t._2._1, t._1));
                }
            })
            .cache();

        System.out.println("Loaded BWD RDD:" + sw.elapsed(TimeUnit.SECONDS));
        sw.stop();


        ResourceLoader resourceLoader = new AnnotationConfigApplicationContext();


        Model datasetModel = ModelFactory.createDefaultModel();
        QueryExecutionFactory dcatQef = FluentQueryExecutionFactory.model(datasetModel).create();
        SparqlPathUtils.readModel(datasetModel, resourceLoader, "classpath:dcat-eswc-training.ttl", Lang.TURTLE);

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
                SparqlService r = MainSparqlPath2.proxySparqlService(coreSparqlService, sparqlStmtParser, prologue);
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


        PropertyFunctionRegistry.get().put(PropertyFunctionKShortestPaths.DEFAULT_IRI, new PropertyFunctionFactoryKShortestPaths(sps ->
            new SparqlKShortestPathFinderSpark(sparkContext, fwdRdd, bwdRdd)));


        Server server = ServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, 7533);
        server.join();


        sparkContext.close();
    }
}



//Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>> frontierIdToAutomaton = new HashMap<>();
//
//// TODO Initialize the frontierIdToAutomaton map
//Broadcast<Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>>> broadcastVar = sparkContext.broadcast(frontierIdToAutomaton);
//
////new SimpleEntry<>(1, "test");
//
////Model m = RDFDataMgr.loadModel("http://cstadler.aksw.org/files/spark/fp7_ict_project_partners_database_2007_2011.nt");
////List<Triple> triples = m.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
//
////System.out.println("FOOOOO" + fileName);
//// Map each subject to corresponding predicate/object pairs
//JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd =
//        sparkContext
//        .textFile(fileName, 5)
//        //.parallelize(triples)
//        .filter(line -> !line.trim().isEmpty() & !line.startsWith("#"))
//        .map(line -> RDFDataMgr.createIteratorTriples(new ByteArrayInputStream(line.getBytes()), Lang.NTRIPLES, "http://example/base").next())
//        .mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
//            private static final long serialVersionUID = -4757627441301230743L;
//            @Override
//            public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
//                    throws Exception {
//                return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
//            }
//        }).cache();
//
//
//JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = fwdRdd.mapToPair(new PairFunction<Tuple2<Node, Tuple2<Node, Node>>, Node, Tuple2<Node, Node>>() {
//    private static final long serialVersionUID = -1567531441301230743L;
//    @Override
//    public Tuple2<Node, Tuple2<Node, Node>> call(
//            Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
//        return new Tuple2<>(t._2._2, new Tuple2<>(t._2._1, t._1));
//    }
//}).cache();
//
////Node startNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/funding/258888-996094068");
//Node startNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/project/258888");
//
//SparqlKShortestPathFinder pathFinder = new SparqlKShortestPathFinderSpark(sparkContext, fwdRdd, bwdRdd);
//
//Path path = PathParser.parse("(!<http://foo>)*", PrefixMapping.Extended);
////Path path = PathParser.parse("<http://fp7-pp.publicdata.eu/ontology/funding>", PrefixMapping.Extended);
//
//
//Iterator<TripletPath<Node, Directed<Node>>> itPaths = pathFinder.findPaths(startNode, Node.ANY, path, 10l);
//while(itPaths.hasNext()) {
//    TripletPath<Node, Directed<Node>> p = itPaths.next();
//    System.out.println("GOT PATH: " + p);
//}
//
//if(false) {
//
////FrontierData<Integer, Node, Node, Integer> start = new Frontier
//
//FrontierItem<Integer, Integer, Node, Node> frontier = new FrontierItem<Integer, Integer, Node, Node>(
//        1,
//        Collections.singleton(1),
//        startNode,
//        false,
//        Node.class);
//
//
//JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> frontierRdd = sparkContext.parallelizePairs(Collections.singletonList(frontier));
//
//
////frontierRdd.filter(new Function<Tuple2<Node, FrontierData<Integer, Integer, Node, Node>>, Boolean>() {
////    @Override
////    public Boolean apply(
////            Tuple2<Node, FrontierData<Integer, Integer, Node, Node>> t) {
////        return t._2.getPathHead().isForward();
////    }
////});
////
//JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> fwdFrontierRdd = frontierRdd;
//
//Function<LabeledEdge<Integer, PredicateClass>, PredicateClass> transToPredicateClass = new Function<LabeledEdge<Integer, PredicateClass>, PredicateClass>() {
//    @Override
//    public PredicateClass call(
//            LabeledEdge<Integer, PredicateClass> v1)
//                    throws Exception {
//        return v1.getLabel();
//    }
//};
//
//
//for(int i = 0; i < 2; ++i) {
//fwdFrontierRdd = NfaExecutionSpark.advanceFrontier(
//        1,
//        fwdFrontierRdd,
//        fwdRdd,
//        false,
//        broadcastVar,
//        transToPredicateClass);
//}
//
//
//System.out.println("GOT THESE PATHS:");
//fwdFrontierRdd.foreach(new VoidFunction<Tuple2<Node,FrontierData<Integer,Integer,Node,Node>>>() {
//    private static final long serialVersionUID = -6067391295525007638L;
//
//    @Override
//    public void call(
//            Tuple2<Node, FrontierData<Integer, Integer, Node, Node>> t)
//                    throws Exception {
//        System.out.println("GOT PATH: " + t._2.getPathHead().getValue().asSimplePath());
//    }
//});
//
//
//Map<Integer, Pair<Number>> dirs = NfaExecutionSpark.analyzeFrontierDir(frontierRdd, broadcastVar, transToPredicateClass);
//System.out.println("DIRS: " + dirs);
//}
// Once we are done with the step, check the frontier for any completed paths






// We can create two frontiers: one that has to be joined with the fwdRdd and one for the bwdRdd.
// The downside is, that after the join, we need to repartition again... oh well...
//
//frontierRdd.partitionBy(new Partitioner() {
//
//    @Override
//    public int getPartition(Object arg0) {
//        // TODO Auto-generated method stub
//        return 0;
//    }
//
//    @Override
//    public int numPartitions() {
//        // TODO Auto-generated method stub
//        return 0;
//    }
//
//});

// Advancing the frontier:
// TODO: the directed in the frontier is meant to indicate whether the path is running backwards
// For every key (node) in the frontier, we perform a join with the edge rdd








// Vertex -> (Tuple2<Directed<NestedPath<>>)

//JavaPairRDD<Node, FrontierItem<Node, Node>> frontierRdd = sparkContext.parallelizePairs(Collections.singletonList(new Tuple2<>(startNode, start)));





//JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = tripleRdd.mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
//    private static final long serialVersionUID = -4757627441301230743L;
//    @Override
//    public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
//            throws Exception {
//        return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
//    }
//});



//fwdRdd.foreach(new VoidFunction<Tuple2<Node, Tuple2<Node, Node>>>() {
//    private static final long serialVersionUID = -2954655113824728223L;
//    @Override
//    public void call(Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
//        //System.out.println("GOT: " + t);
//    }
//});
//



//tripleRdd.
//JavaPairRDD<String, String> x;
//x.cogroup(other)
//JavaRDD


//tripleRdd.saveAsObjectFile("my-triple-rdd.dat");