package org.aksw.jena_sparql_api_sparql_path.spark;

import java.util.function.Predicate;

import org.aksw.jena_sparql_api_sparql_path2.FrontierItem;
import org.aksw.jena_sparql_api_sparql_path2.NestedPath;
import org.aksw.jena_sparql_api_sparql_path2.Nfa;
import org.apache.jena.graph.Node;
import org.apache.spark.api.java.JavaPairRDD;

import scala.Tuple2;

/**
 *
 * @author raven
 */
public class NfaExecutionSpark {



    public static void step() {
        // collectPaths
        // handleClashes


    }

    /**
     *
     * for every frontier entry fe := (vertex, (state, path)) in the frontier {
     *   pc = retrieve the propertyclass leading to successor states in the nfa based on the current state
     *
     *   // Determine the most recent predicate of the path, check the nfa for successor predicates,
     *   // and check the join summary for those joins that may lead to the target (in regard to the nfa)
     *   lastPredicate = getLastPredicate(path);
     *
     *
     *   for nodes in the propertyClass.fwdNodes {
     *
     *   }
     *
     *
     * }
     *
     *
     *
     * @param <S> nfa state type
     * @param <T> nfa transition type
     * @param <V> data vertex type
     * @param <E> data edge type
     */
    public static <S, T, V, E> JavaPairRDD<V, Tuple2<S, NestedPath<V, E>>> advanceFrontier(
            int frontierId, // the id of the frontier which to advance
            JavaPairRDD<V, Tuple2<S, NestedPath<V, E>>> frontier,
            Nfa<V, E> nfa,
            JavaPairRDD<Node, FrontierItem<S, Node, Node>> frontierRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> fwdRdd,
            JavaPairRDD<Node, Tuple2<Node, Node>> bwdRdd,
            boolean reversePropertyDirection,
            Predicate<T> isEpsilon,
            Object edgeRdd) {

        //frontierRdd.mapPartitions(f)

        //frontierRdd.agg


        // This join associates each frontier item with the set of edges
        JavaPairRDD<Node, Tuple2<FrontierItem<S, Node, Node>, Tuple2<Node, Node>>> joinedRdd = frontierRdd.join(fwdRdd);





        //
        //joinedRdd.mapToPair(f)



        //frontier.j



        return frontier;
            //
            //Function<DirectedProperty<T>, Function<Iterable<V>, Map<V, Graphlet<V, E>>>> transitionToNodesToGraphlets) {

    }
}
