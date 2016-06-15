package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.aksw.jena_sparql_api.sparql_path2.LabeledEdge;
import org.aksw.jena_sparql_api.sparql_path2.NestedPath;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.PathCompiler;
import org.aksw.jena_sparql_api.sparql_path2.PredicateClass;
import org.aksw.jena_sparql_api.sparql_path2.SparqlKShortestPathFinder;
import org.aksw.jena_sparql_api.sparql_path2.TripletPath;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api_sparql_path.spark.NfaExecutionSpark;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;
import org.apache.spark.Partitioner;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;

import scala.Tuple2;

public class SparqlKShortestPathFinderSpark
    implements SparqlKShortestPathFinder
{
    protected final JavaSparkContext sparkContext;
    protected final JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd;
    protected final JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd;
    //protected final Partitioner nodePartitioner;

    public SparqlKShortestPathFinderSpark(
            JavaSparkContext sparkContext,
            JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd
            //Partitioner nodePartitioner
            ) {
        this.sparkContext = sparkContext;
        this.fwdRdd = fwdRdd;
        this.bwdRdd = bwdRdd;
        //this.nodePartitioner = nodePartitioner;
    }


    public static JavaRDD<NestedPath<Node, Node>> exec(
            JavaSparkContext sparkContext,
            JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd,
            //Partitioner nodePartitioner,
            Node start,
            Node end,
            Path path,
            Long k) {

        Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>> frontierIdToAutomaton = new HashMap<>();


        Integer nfaId = 1;
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);


        frontierIdToAutomaton.put(nfaId, nfa);

        // TODO Initialize the frontierIdToAutomaton map
        Broadcast<Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>>> idToNfa = sparkContext.broadcast(frontierIdToAutomaton);

        Map<Integer, Node> frontierIdToTarget = new HashMap<>();
        frontierIdToTarget.put(nfaId, end);
        Broadcast<Map<Integer, Node>> idToTarget = sparkContext.broadcast(frontierIdToTarget);




        // Set up the frontier
        FrontierItem<Integer, Integer, Node, Node> frontier = new FrontierItem<Integer, Integer, Node, Node>(
                1,
                nfa.getStartStates(),
                start,
                false,
                Node.class);

        Partitioner nodePartitioner = fwdRdd.partitioner().get();

        JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> frontierRdd = sparkContext
                .parallelizePairs(Collections.singletonList(frontier))
                .partitionBy(nodePartitioner)
                .persist(StorageLevel.MEMORY_AND_DISK_SER());

        JavaPairRDD<Integer, NestedPath<Node, Node>> currentPathRdd = null;


        Function<LabeledEdge<Integer, PredicateClass>, PredicateClass> transToPredicateClass = new Function<LabeledEdge<Integer, PredicateClass>, PredicateClass>() {
            private static final long serialVersionUID = 23901355501L;
            @Override
            public PredicateClass call(
                    LabeledEdge<Integer, PredicateClass> v1)
                            throws Exception {
                return v1.getLabel();
            }
        };

        long foundPathCount;
        do {
            frontierRdd.count();

            JavaPairRDD<Integer, NestedPath<Node, Node>> pathContribRdd = NfaExecutionSpark
                    .collectPaths(frontierRdd, idToNfa, idToTarget, transToPredicateClass);
                    //.persist(StorageLevel.MEMORY_AND_DISK_SER());

            //System.out.println("FRONTIER SIZE: " + frontierRdd.count());

            // merge the paths
            //JavaPairRDD<Integer, NestedPath<Node, Node>> nextFoundPathsRdd;
            JavaPairRDD<Integer, NestedPath<Node, Node>> nextPathRdd;
            if(currentPathRdd == null) {
                nextPathRdd = pathContribRdd;
                        //.persist(StorageLevel.MEMORY_AND_DISK_SER());
            } else {
                nextPathRdd = pathContribRdd
                        .union(currentPathRdd)
                        .persist(StorageLevel.MEMORY_AND_DISK_SER());

            }

            foundPathCount = nextPathRdd.count();

            if(currentPathRdd != null)
            {
                currentPathRdd.unpersist(false);
            }
            currentPathRdd = nextPathRdd;


            Map<Integer, Pair<Number>> stats = NfaExecutionSpark.analyzeFrontierDir(frontierRdd, idToNfa, transToPredicateClass);

            //System.out.println("Frontier analysis: " + stats);

            Pair<Number> agg = stats.values().stream().reduce(
                    new Pair<Number>(0, 0),
                    (a, b) -> new Pair<>(a.getKey().longValue() + b.getKey().longValue(), a.getValue().longValue() + b.getValue().longValue()));

            boolean requiresFwdJoin = agg.getKey().longValue() > 0l;
            boolean requiresBwdJoin = agg.getValue().longValue() > 0l;

            //System.out.println("Joins: " + requiresFwdJoin + " " + requiresBwdJoin);

            JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> fwdFrontierRdd = null;
            if(requiresFwdJoin) {
                fwdFrontierRdd = NfaExecutionSpark
                        .advanceFrontier(
                            sparkContext,
                            1,
                            frontierRdd,
                            fwdRdd,
                            false,
                            idToNfa,
                            transToPredicateClass
                        );
                        //.persist(StorageLevel.MEMORY_AND_DISK_SER());
                //System.out.println("FWD FRONTIER SIZE: " + fwdFrontierRdd.cou);
            }

            JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> bwdFrontierRdd = null;
            if(requiresBwdJoin) {
                bwdFrontierRdd = NfaExecutionSpark
                        .advanceFrontier(
                            sparkContext,
                            1,
                            frontierRdd,
                            bwdRdd,
                            true,
                            idToNfa,
                            transToPredicateClass
                        );
                        //.persist(StorageLevel.MEMORY_AND_DISK_SER());
                //bwdFrontierRdd.count();
            }

            JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> nextFrontierRdd;
            boolean doUnpersist = false;
            if(fwdFrontierRdd == null) {
                nextFrontierRdd = bwdFrontierRdd;
            } else {
                if(bwdFrontierRdd == null) {
                    nextFrontierRdd = fwdFrontierRdd;
                } else {
                    nextFrontierRdd = fwdFrontierRdd
                            .union(bwdFrontierRdd)
                            .persist(StorageLevel.MEMORY_AND_DISK_SER());

                    //if(true) { throw new RuntimeException("not expected"); }
                    doUnpersist = true;
                }
            }

//            if(nextFrontierRdd != null) {
//                nextFrontierRdd = nextFrontierRdd
//                  //.partitionBy(nodePartitioner)
//                  .persist(StorageLevel.MEMORY_AND_DISK_SER());
//            }
//
//                    ? bwdFrontierRdd
//                    : (bwdFrontierRdd == null
//                        ? fwdFrontierRdd
//                        : fwdFrontierRdd
//                            .union(bwdFrontierRdd));
//                            //.persist(StorageLevel.MEMORY_AND_DISK());
//
//            nextFrontierRdd = nextFrontierRdd
////                .partitionBy(nodePartitioner)
//                .persist(StorageLevel.MEMORY_AND_DISK_SER());

//            if(nextFrontierRdd != null) {
//                nextFrontierRdd.persist(StorageLevel.MEMORY_AND_DISK());
//            }


//            long frontierSize = nextFrontierRdd != null
//                    ? nextFrontierRdd.isE
//            if() {
                //nextFrontierRdd.first();
                //long frontierSize = nextFrontierRdd.count();
                //System.out.println("Next Frontier size: " + frontierSize);
//            }

            frontierRdd.unpersist(false);

            if(doUnpersist) {
                fwdFrontierRdd.unpersist(false);
                bwdFrontierRdd.unpersist(false);
            }

            frontierRdd = nextFrontierRdd;

        } while (foundPathCount <= k && frontierRdd != null && !frontierRdd.isEmpty());


//        rdd
//        .sortBy(_.createDate.getTime)
//        .zipWithIndex
//        .filter{case (_, idx) => idx < n}
//        .keys


        //JavaPairRDD<Integer, NestedPath<Node, Node>> segmentPathRdd = NfaExecutionSpark.collectPaths(fwdFrontierRdd, idToNfa);

        JavaRDD<NestedPath<Node, Node>> finalPathRdd = currentPathRdd
            .map(new Function<Tuple2<Integer,NestedPath<Node,Node>>, NestedPath<Node, Node>>() {
                private static final long serialVersionUID = 234902531915L;
                @Override
                public NestedPath<Node, Node> call(
                        Tuple2<Integer, NestedPath<Node, Node>> v1)
                                throws Exception {
                    NestedPath<Node, Node> r = v1._2;
                    return r;
                }
            })
            .sortBy(new Function<NestedPath<Node,Node>, Integer>() {
                private static final long serialVersionUID = 331039331952L;
                @Override
                public Integer call(NestedPath<Node, Node> v1) throws Exception {
                    return v1.getLength();
                }
            }, true, 10) // TODO What is a good number of partitions?
            .zipWithIndex()
            .filter(new Function<Tuple2<NestedPath<Node,Node>,Long>, Boolean>() {
                private static final long serialVersionUID = 47129253090252L;
                @Override
                public Boolean call(Tuple2<NestedPath<Node, Node>, Long> v1)
                        throws Exception {
                    long index = v1._2();
                    return index < k;
                }
            })
            .keys()
            .persist(StorageLevel.MEMORY_AND_DISK_SER());

        idToNfa.destroy();
        idToTarget.destroy();

        return finalPathRdd;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start,
            Node end, Path pathExpr, Long k) {

        JavaRDD<NestedPath<Node, Node>> finalPathRdd = exec(sparkContext, fwdRdd, bwdRdd, start, end, pathExpr, k);

        Iterator<NestedPath<Node, Node>> tmp = finalPathRdd.toLocalIterator();

        Iterable<NestedPath<Node, Node>> i = () -> tmp;
        Iterator<TripletPath<Node, Directed<Node>>> result = StreamSupport.stream(i.spliterator(), false)
                .map(e -> e.asSimpleDirectedPath()).iterator();


        //Stream.of
        return result;
    }
}
