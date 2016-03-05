package org.aksw.jena_sparql_api_sparql_path2;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api_sparql_path.spark.NfaExecutionSpark;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public class MainJavaSparkTest {

    private static final Logger logger = LoggerFactory.getLogger(MainJavaSparkTest.class);

    public static void main(String[] args) {


        if (args.length < 1) {
            logger.error("=> wrong parameters number");
            System.err.println("Usage: FileName <path-to-files> <output-path>");
            System.exit(1);
        }

        String fileName = args[0];
        String sparkMasterHost = args.length >= 2 ? args[1] : "local[2]";

        SparkConf sparkConf = new SparkConf()
                .setAppName("BDE-readRDF")
                .setMaster(sparkMasterHost)
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        // TODO Replace with the regex automaton

        Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>> frontierIdToAutomaton = new HashMap<>();

        // TODO Initialize the frontierIdToAutomaton map
        Broadcast<Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>>> broadcastVar = sparkContext.broadcast(frontierIdToAutomaton);

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


        Node startNode = NodeFactory.createURI("http://fp7-pp.publicdata.eu/resource/funding/258888-996094068");
        //FrontierData<Integer, Node, Node, Integer> start = new Frontier

        FrontierItem<Integer, Integer, Node, Node> frontier = new FrontierItem<Integer, Integer, Node, Node>(
                1,
                Collections.singleton(1),
                startNode,
                false,
                Node.class);


        JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> frontierRdd = sparkContext.parallelizePairs(Collections.singletonList(frontier));


//        frontierRdd.filter(new Function<Tuple2<Node, FrontierData<Integer, Integer, Node, Node>>, Boolean>() {
//            @Override
//            public Boolean apply(
//                    Tuple2<Node, FrontierData<Integer, Integer, Node, Node>> t) {
//                return t._2.getPathHead().isForward();
//            }
//        });
//
        JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> fwdFrontierRdd = frontierRdd;

        for(int i = 0; i < 2; ++i) {
        fwdFrontierRdd = NfaExecutionSpark.advanceFrontier(
                1,
                fwdFrontierRdd,
                fwdRdd,
                bwdRdd,
                false,
                broadcastVar);
                //LabeledEdgeImpl::<Node>isEpsilon);
        }


        System.out.println("GOT THESE PATHS:");
        fwdFrontierRdd.foreach(new VoidFunction<Tuple2<Node,FrontierData<Integer,Integer,Node,Node>>>() {
            private static final long serialVersionUID = -6067391295525007638L;

            @Override
            public void call(
                    Tuple2<Node, FrontierData<Integer, Integer, Node, Node>> t)
                            throws Exception {
                System.out.println("GOT PATH: " + t._2.getPathHead().getValue().asSimplePath());
            }
        });


        Map<Integer, Pair<Number>> dirs = NfaExecutionSpark.analyzeFrontierDir(frontierRdd, broadcastVar);
        System.out.println("DIRS: " + dirs);

        // Once we are done with the step, check the frontier for any completed paths






        // We can create two frontiers: one that has to be joined with the fwdRdd and one for the bwdRdd.
        // The downside is, that after the join, we need to repartition again... oh well...
//
//        frontierRdd.partitionBy(new Partitioner() {
//
//            @Override
//            public int getPartition(Object arg0) {
//                // TODO Auto-generated method stub
//                return 0;
//            }
//
//            @Override
//            public int numPartitions() {
//                // TODO Auto-generated method stub
//                return 0;
//            }
//
//        });

        // Advancing the frontier:
        // TODO: the directed in the frontier is meant to indicate whether the path is running backwards
        // For every key (node) in the frontier, we perform a join with the edge rdd








        // Vertex -> (Tuple2<Directed<NestedPath<>>)

        //JavaPairRDD<Node, FrontierItem<Node, Node>> frontierRdd = sparkContext.parallelizePairs(Collections.singletonList(new Tuple2<>(startNode, start)));





//        JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd = tripleRdd.mapToPair(new PairFunction<Triple, Node, Tuple2<Node, Node>>() {
//            private static final long serialVersionUID = -4757627441301230743L;
//            @Override
//            public Tuple2<Node, Tuple2<Node, Node>> call(Triple t)
//                    throws Exception {
//                return new Tuple2<>(t.getSubject(), new Tuple2<>(t.getPredicate(), t.getObject()));
//            }
//        });



//        fwdRdd.foreach(new VoidFunction<Tuple2<Node, Tuple2<Node, Node>>>() {
//            private static final long serialVersionUID = -2954655113824728223L;
//            @Override
//            public void call(Tuple2<Node, Tuple2<Node, Node>> t) throws Exception {
//                //System.out.println("GOT: " + t);
//            }
//        });
//



        //tripleRdd.
        //JavaPairRDD<String, String> x;
        //x.cogroup(other)
//        JavaRDD


        //tripleRdd.saveAsObjectFile("my-triple-rdd.dat");

        sparkContext.close();
    }
}
