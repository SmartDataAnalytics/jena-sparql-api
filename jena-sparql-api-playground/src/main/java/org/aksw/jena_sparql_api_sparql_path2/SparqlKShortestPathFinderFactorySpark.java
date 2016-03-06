package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aksw.jena_sparql_api_sparql_path.spark.NfaExecutionSpark;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import scala.Tuple2;

public class SparqlKShortestPathFinderFactorySpark
    implements SparqlKShortestPathFinder
{
    protected JavaSparkContext sparkContext;
    protected JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd;
    protected JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd;

    public SparqlKShortestPathFinderFactorySpark(JavaSparkContext sparkContext, JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd, JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd) {
        this.sparkContext = sparkContext;
        this.fwdRdd = fwdRdd;
        this.bwdRdd = bwdRdd;
    }


    public static JavaRDD<NestedPath<Node, Node>> exec(
            JavaSparkContext sparkContext2,
            JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd,
            Node start,
            Node end,
            Path pathExpr,
            Long k) {

        Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>> frontierIdToAutomaton = new HashMap<>();

        // TODO Initialize the frontierIdToAutomaton map
        Broadcast<Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>>> broadcastVar = sparkContext2.broadcast(frontierIdToAutomaton);

        // Set up the frontier
        FrontierItem<Integer, Integer, Node, Node> frontier = new FrontierItem<Integer, Integer, Node, Node>(
                1,
                Collections.singleton(1),
                start,
                false,
                Node.class);


        JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> frontierRdd = sparkContext2.parallelizePairs(Collections.singletonList(frontier));

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


        JavaPairRDD<Integer, NestedPath<Node, Node>> segmentPathRdd = NfaExecutionSpark.collectPaths(fwdFrontierRdd, broadcastVar);

        JavaRDD<NestedPath<Node, Node>> finalPathRdd = segmentPathRdd.map(new Function<Tuple2<Integer,NestedPath<Node,Node>>, NestedPath<Node, Node>>() {
            private static final long serialVersionUID = 234902531915L;
            @Override
            public NestedPath<Node, Node> call(
                    Tuple2<Integer, NestedPath<Node, Node>> v1)
                            throws Exception {
                NestedPath<Node, Node> r = v1._2;
                return r;
            }
        });

        return finalPathRdd;
    }

    @Override
    public Iterator<NestedPath<Node, Node>> findPaths(Node start,
            Node end, Path pathExpr, Long k) {

        JavaRDD<NestedPath<Node, Node>> finalPathRdd = exec(sparkContext, fwdRdd, bwdRdd, start, end, pathExpr, k);

        Iterator<NestedPath<Node, Node>> result = finalPathRdd.toLocalIterator();
        return result;
    }
}
