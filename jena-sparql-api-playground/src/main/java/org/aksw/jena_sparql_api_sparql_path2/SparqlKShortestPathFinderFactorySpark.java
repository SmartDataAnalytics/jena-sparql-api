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
            JavaSparkContext sparkContext,
            JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd,
            Node start,
            Node end,
            Path path,
            Long k) {

        Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>> frontierIdToAutomaton = new HashMap<>();


        Integer nfaId = 1;
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathExecutionUtils.compileToNfa(path);


        frontierIdToAutomaton.put(nfaId, nfa);

        // TODO Initialize the frontierIdToAutomaton map
        Broadcast<Map<Integer, Nfa<Integer, LabeledEdge<Integer, PredicateClass>>>> idToNfa = sparkContext.broadcast(frontierIdToAutomaton);


        // Set up the frontier
        FrontierItem<Integer, Integer, Node, Node> frontier = new FrontierItem<Integer, Integer, Node, Node>(
                1,
                nfa.getStartStates(),
                start,
                false,
                Node.class);


        JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> frontierRdd = sparkContext.parallelizePairs(Collections.singletonList(frontier));

        JavaPairRDD<Integer, NestedPath<Node, Node>> foundPathsRdd = null;


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
            JavaPairRDD<Integer, NestedPath<Node, Node>> nextPathRdd = NfaExecutionSpark.collectPaths(frontierRdd, idToNfa);

            // merge the paths
            foundPathsRdd = foundPathsRdd == null ? nextPathRdd : foundPathsRdd.union(nextPathRdd).cache();


            Map<Integer, Pair<Number>> stats = NfaExecutionSpark.analyzeFrontierDir(frontierRdd, idToNfa, transToPredicateClass);

            Pair<Number> agg = stats.values().stream().reduce(
                    new Pair<Number>(0, 0),
                    (a, b) -> new Pair<>(a.getKey().longValue() + b.getKey().longValue(), a.getValue().longValue() + b.getValue().longValue()));

            boolean requiresFwdJoin = agg.getKey().longValue() > 0l;
            boolean requiresBwdJoin = agg.getValue().longValue() > 0l;

            JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> fwdFrontierRdd = null;
            if(requiresFwdJoin) {
                fwdFrontierRdd = NfaExecutionSpark.advanceFrontier(
                        1,
                        frontierRdd,
                        fwdRdd,
                        false,
                        idToNfa,
                        transToPredicateClass
                        );
            }

            JavaPairRDD<Node, FrontierData<Integer, Integer, Node, Node>> bwdFrontierRdd = null;
            if(requiresBwdJoin) {
                bwdFrontierRdd = NfaExecutionSpark.advanceFrontier(
                        1,
                        frontierRdd,
                        bwdRdd,
                        true,
                        idToNfa,
                        transToPredicateClass
                        );
            }

            frontierRdd = fwdFrontierRdd == null
                    ? bwdFrontierRdd
                    : (bwdFrontierRdd == null
                        ? fwdFrontierRdd
                        : fwdFrontierRdd.union(bwdFrontierRdd));

            foundPathCount = foundPathsRdd.count();
        } while (foundPathCount <= k && frontierRdd != null);


        //JavaPairRDD<Integer, NestedPath<Node, Node>> segmentPathRdd = NfaExecutionSpark.collectPaths(fwdFrontierRdd, idToNfa);

        JavaRDD<NestedPath<Node, Node>> finalPathRdd = foundPathsRdd.map(new Function<Tuple2<Integer,NestedPath<Node,Node>>, NestedPath<Node, Node>>() {
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
