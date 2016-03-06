package org.aksw.jena_sparql_api_sparql_path.spark;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api_sparql_path2.Directed;
import org.aksw.jena_sparql_api_sparql_path2.FrontierData;
import org.aksw.jena_sparql_api_sparql_path2.FrontierItem;
import org.aksw.jena_sparql_api_sparql_path2.MapUtils;
import org.aksw.jena_sparql_api_sparql_path2.NestedPath;
import org.aksw.jena_sparql_api_sparql_path2.Nfa;
import org.aksw.jena_sparql_api_sparql_path2.Pair;
import org.aksw.jena_sparql_api_sparql_path2.ParentLink;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;

import jersey.repackaged.com.google.common.collect.Sets;
import scala.Tuple2;

class JoinStats<V, E> {
    protected E predicate;
    protected V target;

    protected Number totalCount;
    protected Number sourceCount;
    protected Number targetCount;

}

/**
 *
 * @author raven
 */
public class NfaExecutionSpark {



    public static void step() {
        // collectPaths
        // handleClashes


    }

    public static <V, E> JavaPairRDD<V, JoinStats<V, E>> createJoinSummary(JavaPairRDD<V, Tuple2<E, V>> rdd) {
        //rdd.
        return null;
    }


    /**
     * Iterate the frontier and collect all matching paths into a new RDD
     * This is a filter operation for matching paths + a map to yield the path + unique
     *
     */
    public static <I, S, T, V, E> JavaPairRDD<I, NestedPath<V, E>> collectPaths(JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd, Broadcast<Map<I, Nfa<S, T>>> idToNfa) {
        JavaPairRDD<I, NestedPath<V, E>> result = frontierRdd.filter(new Function<Tuple2<V,FrontierData<I,S,V,E>>, Boolean>() {
            private static final long serialVersionUID = 7576754196298849489L;

            @Override
            public Boolean call(Tuple2<V, FrontierData<I, S, V, E>> v1)
                    throws Exception {
                // yield all paths that have reached the corresponding nfa's accepting states
                FrontierData<I, S, V, E> frontierData = v1._2;
                I nfaId = frontierData.getFrontierId();
                NestedPath<V, E> nestedPath = frontierData.getPathHead().getValue();
                Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);

//                Set<S> states = frontierData.getStates();
//
//                Set<S> tmp = Sets.intersection(nfa.getEndStates(), states);
//                boolean isAccepting = !tmp.isEmpty();

                boolean isAccepting = true;

                // TODO Check if the path's target is accepted
                //nestedPath.getCurrent().equals(target);


                return isAccepting;
            }
        }).mapToPair(new PairFunction<Tuple2<V, FrontierData<I, S, V, E>>, I, NestedPath<V, E>>() {
            private static final long serialVersionUID = 94392315173L;
            @Override
            public Tuple2<I, NestedPath<V, E>> call(
                    Tuple2<V, FrontierData<I, S, V, E>> t) throws Exception {
                FrontierData<I, S, V, E> frontierData = t._2;
                I frontierId = frontierData.getFrontierId();
                NestedPath<V, E> nestedPath = frontierData.getPathHead().getValue();

                Tuple2<I, NestedPath<V, E>> r = new Tuple2<>(frontierId, nestedPath);
                return r;
            }
        }).distinct();

        return result;
    }



    /**
     * Analyse the frontier for whether a join with the fwd or bwd rdss is necessary
     *
     * Returns a map for each nfa to the number of inbound / outbound frontier items
     *
     * @return
     */
    public  static <I, S, T, V, E> Map<I, Pair<Number>> analyzeFrontierDir(JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd, Broadcast<Map<I, Nfa<S, T>>> idToNfa) {

        //frontierRdd.toLocalIterator();
        //frontierRdd.collect()
        Map<I, Pair<Number>> result = frontierRdd.aggregate(
                (Map<I, Pair<Number>>)new HashMap<I, Pair<Number>>(),
                new Function2<Map<I, Pair<Number>>, Tuple2<V, FrontierData<I, S, V, E>>, Map<I, Pair<Number>>>() {
                    private static final long serialVersionUID = -6994126765252908625L;

                    @Override
                    public Map<I, Pair<Number>> call(Map<I, Pair<Number>> v1,
                            Tuple2<V, FrontierData<I, S, V, E>> v2) throws Exception {

                        FrontierData<I, S, V, E> frontierData = v2._2;
                        I nfaId = frontierData.getFrontierId();
                        Set<S> states = frontierData.getStates();
                        Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);

                        // check the transitions
                        //Map<I, Pair<Number>> result = new HashMap<>();
                        Map<I, Pair<Number>> r = Collections.singletonMap(nfaId, new Pair<>(1, 1));

                        return r;
                    }
                },
                new Function2<Map<I, Pair<Number>>, Map<I, Pair<Number>>, Map<I, Pair<Number>>>() {
                    private static final long serialVersionUID = 4578518485699245971L;

                    @Override
                    public Map<I, Pair<Number>> call(Map<I, Pair<Number>> v1,
                            Map<I, Pair<Number>> v2) throws Exception {

                        Map<I, Pair<Number>> r = MapUtils.mergeMaps(
                                v1, v2, (a, b) -> new Pair<Number>(
                                        a.getKey().longValue() + b.getKey().longValue(),
                                        a.getValue().longValue() + b.getValue().longValue()));
                        return r;
                    }
                });

        return result;
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
            JavaPairRDD<V, Tuple2<E, V>> rdd,
//            JavaPairRDD<V, Tuple2<E, V>> bwdRdd,
            //boolean reversePropertyDirection,
            boolean isReverseRdd,
            Broadcast<Map<I, Nfa<S, T>>> idToNfa
            )
     //       Predicate<T> isEpsilon)
    {

        JavaPairRDD<V, FrontierData<I, S, V, E>> result = frontierRdd
            .join(rdd)
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
