package org.aksw.jena_sparql_api_sparql_path.spark;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api_sparql_path2.Directed;
import org.aksw.jena_sparql_api_sparql_path2.FrontierData;
import org.aksw.jena_sparql_api_sparql_path2.FrontierItem;
import org.aksw.jena_sparql_api_sparql_path2.LabeledEdge;
import org.aksw.jena_sparql_api_sparql_path2.NestedPath;
import org.aksw.jena_sparql_api_sparql_path2.Nfa;
import org.aksw.jena_sparql_api_sparql_path2.ParentLink;
import org.aksw.jena_sparql_api_sparql_path2.PredicateClass;
import org.apache.jena.graph.Node;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;

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
    public static <I, S, T, V, E> JavaPairRDD<V, FrontierData<I, S, V, E>> advanceFrontier(
            I frontierId, // the id of the frontier which to advance
            //Nfa<V, E> nfa,
            JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd,
            JavaPairRDD<V, Tuple2<E, V>> fwdRdd,
            JavaPairRDD<V, Tuple2<E, V>> bwdRdd,
            boolean reversePropertyDirection,
            Broadcast<Map<I, Nfa<S, T>>> idToNfa
            )
     //       Predicate<T> isEpsilon)
    {

        JavaPairRDD<V, FrontierData<I, S, V, E>> result = frontierRdd
            .join(fwdRdd)
            .filter(new Function<Tuple2<V, Tuple2<FrontierData<I, S, V, E>,Tuple2<E,V>>>, Boolean>() {
                private static final long serialVersionUID = 12351375937L;
                @Override
                public Boolean call(
                        Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>> t)
                                throws Exception {
                    //Map<I, Nfa<S, LabeledEdge<S, PredicateClass>>> idToNfa = null;// (Map<I, Nfa<S, LabeledEdge<S, PredicateClass>>>)broadcastVar.getValue();

                    FrontierData<I, S, V, E> frontierData = t._2._1;
                    I nfaId = frontierData.getFrontierId();
                    E p = t._2._2._1;
                    V o = t._2._2._2;

                    Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);

                    // Check whether the current p and o are acceptable according to the nfa
                    return true;
                }
            })
            .mapToPair(new PairFunction<Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>>, V, FrontierData<I, S, V, E>>() {
                    //new Function<Tuple2<V, Tuple2<FrontierData<I, S, V, E>,Tuple2<E, V>>>, FrontierItem<I, S, V, E>>() {
                private static final long serialVersionUID = 1312323951L;

                @Override
                public Tuple2<V, FrontierData<I, S, V, E>> call(
                        Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>> t)
                                throws Exception {

                    FrontierData<I, S, V, E> frontierData = t._2._1;
                    I nfaId = frontierData.getFrontierId();
                    E p = t._2._2._1;
                    V o = t._2._2._2;

                    //hack
                    Object tmp = 123;
                    Set<S> nextStates = Collections.singleton((S)tmp);

                    Directed<NestedPath<V, E>> pathHead = frontierData.getPathHead();
                    NestedPath<V, E> nestedPath = frontierData.getPathHead().getValue();

                    NestedPath<V, E> nextPath = new NestedPath<>(new ParentLink<>(nestedPath, new Directed<>(p, false)), o);
                    Directed<NestedPath<V, E>> nextPathHead = new Directed<>(nextPath, pathHead.isReverse());

                    FrontierItem<I, S, V, E> result = new FrontierItem<I, S, V, E>(nfaId, nextStates, nextPathHead);

                    return result;
                }
            });

        return result;
    }
}
