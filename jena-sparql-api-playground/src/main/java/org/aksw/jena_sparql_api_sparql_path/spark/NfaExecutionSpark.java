package org.aksw.jena_sparql_api_sparql_path.spark;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api_sparql_path2.DirectedProperty;
import org.aksw.jena_sparql_api_sparql_path2.Graphlet;
import org.aksw.jena_sparql_api_sparql_path2.NestedPath;
import org.aksw.jena_sparql_api_sparql_path2.Nfa;
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
            JavaPairRDD<V, Tuple2<S, NestedPath<V, E>>> frontier,
            Nfa<V, E> nfa,
            boolean reversePropertyDirection,
            Predicate<T> isEpsilon,
            Object edgeRdd) {

        // algo sketch:
        //
        //


        //frontier.j



        return frontier;
            //
            //Function<DirectedProperty<T>, Function<Iterable<V>, Map<V, Graphlet<V, E>>>> transitionToNodesToGraphlets) {

    }
}
