package org.aksw.jena_sparql_api_sparql_path.spark;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api_sparql_path2.FrontierData;
import org.aksw.jena_sparql_api_sparql_path2.FrontierItem;
import org.aksw.jena_sparql_api_sparql_path2.JGraphTUtils;
import org.aksw.jena_sparql_api_sparql_path2.MapUtils;
import org.aksw.jena_sparql_api_sparql_path2.NestedPath;
import org.aksw.jena_sparql_api_sparql_path2.Nfa;
import org.aksw.jena_sparql_api_sparql_path2.ParentLink;
import org.aksw.jena_sparql_api_sparql_path2.PredicateClass;
import org.aksw.jena_sparql_api_sparql_path2.ValueSet;
import org.apache.jena.graph.Node;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.Sets;

import scala.Tuple2;
import scala.tools.nsc.settings.AdvancedScalaSettings.X;

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


    public static <T> PredicateClass getPredicateClass(Function<T, PredicateClass> transToPredicateClass, T trans) {
        PredicateClass result;
        try {
            result = transToPredicateClass.call(trans);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Iterate the frontier and collect all matching paths into a new RDD
     * This is a filter operation for matching paths + a map to yield the path + unique
     *
     */
    public static <I, S, T, V, E> JavaPairRDD<I, NestedPath<V, E>> collectPaths(
            JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd,
            Broadcast<Map<I, Nfa<S, T>>> idToNfaBc,
            Broadcast<Map<I, V>> idToTargetBc,
            Function<T, PredicateClass> transToPredicateClass) {


        JavaPairRDD<I, NestedPath<V, E>> result = frontierRdd
            .filter(new Function<Tuple2<V,FrontierData<I,S,V,E>>, Boolean>() {
                private static final long serialVersionUID = 7576754196298849489L;

                @Override
                public Boolean call(Tuple2<V, FrontierData<I, S, V, E>> v1)
                        throws Exception {

                    Map<I, Nfa<S, T>> idToNfa = idToNfaBc.getValue();
                    Map<I, V> idToTarget = idToTargetBc.getValue();

                    // yield all paths that have reached the corresponding nfa's accepting states
                    FrontierData<I, S, V, E> frontierData = v1._2;
                    I nfaId = frontierData.getFrontierId();
                    //NestedPath<V, E> nestedPath = frontierData.getPathHead().getValue();
                    V targetNode = idToTarget.get(nfaId);

                    Nfa<S, T> nfa = idToNfa.get(nfaId);//idToNfa.getValue().get(nfaId);
                    Set<S> endStates = nfa.getEndStates();
                    DirectedGraph<S, T> graph = nfa.getGraph();

                    Set<S> rawStates = frontierData.getStates();

                    Set<S> states = JGraphTUtils.transitiveGet(graph, rawStates, 1, trans -> getPredicateClass(transToPredicateClass, trans) == null);
    //
                    Set<S> tmp = Sets.intersection(endStates, states);
                    boolean isAccepting = !tmp.isEmpty();

                    boolean isTarget = targetNode != null ? frontierData.getPathHead().getValue().getCurrent().equals(targetNode) : true;
                    boolean r = isAccepting && isTarget;

    //                boolean isAccepting = true;

                    // TODO Check if the path's target is accepted


                    return r;
                }
            })
            .mapToPair(new PairFunction<Tuple2<V, FrontierData<I, S, V, E>>, I, NestedPath<V, E>>() {
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
            })
            .distinct()
            .persist(StorageLevel.MEMORY_AND_DISK_SER());

        return result;
    }



    /**
     * Analyse the frontier for whether a join with the fwd or bwd rdss is necessary
     *
     * Returns a map for each nfa to the number of inbound / outbound frontier items
     *
     * @return
     */
    public  static <I, S, T, V, E> Map<I, Pair<Number>> analyzeFrontierDir(
            JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd,
            Broadcast<Map<I, Nfa<S, T>>> idToNfa,
            Function<T, PredicateClass> transToPredicateClass // null implies epsilon transition
            ) {

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
                        Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);
                        DirectedGraph<S, T> graph = nfa.getGraph();

                        Set<S> states = frontierData.getStates();

                        Set<T> transitions = JGraphTUtils.resolveTransitions(graph, trans -> getPredicateClass(transToPredicateClass, trans) == null, states, false);

                        boolean requiresFwdJoin = transitions.stream().anyMatch(trans -> !getPredicateClass(transToPredicateClass, trans).getFwdNodes().isEmpty());
                        boolean requiresBwdJoin = transitions.stream().anyMatch(trans -> !getPredicateClass(transToPredicateClass, trans).getBwdNodes().isEmpty());

                        Map<I, Pair<Number>> r = Collections.singletonMap(nfaId, new Pair<>(requiresFwdJoin ? 1 : 0, requiresBwdJoin ? 1 : 0));

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


    public static <K, V, W> JavaPairRDD<K, Tuple2<V, W>> autoJoin(JavaSparkContext sc, JavaPairRDD<K, V> a, JavaPairRDD<K, W> b) {


        JavaPairRDD<K, Tuple2<V, W>> result; // = a.join(b);
        Map<K, V> map = a.collectAsMap();
        Broadcast<Map<K, V>> bv = sc.broadcast(map);

//        b.mapPartitionsToPair(new PairFlatMapFunction<Iterator<Tuple2<K,W>>, K, V>() {
//
//            @Override
//            public Iterable<Tuple2<K, V>> call(Iterator<Tuple2<K, W>> t)
//                    throws Exception {
//                // TODO Auto-generated method stub
//                return null;
//            }
//        });


        result = b.mapToPair(new PairFunction<Tuple2<K, W>, K, Tuple2<V, W>>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<K, Tuple2<V, W>> call(Tuple2<K, W> t)
                    throws Exception {
                K k = t._1;
                W w = t._2;
                V v = bv.getValue().get(k);

                Tuple2<K, Tuple2<V, W>> r = v == null
                        ? null
                        : new Tuple2<>(k, new Tuple2<>(v, w));

                return r;
            }
        }).filter(new Function<Tuple2<K,Tuple2<V,W>>, Boolean>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean call(Tuple2<K, Tuple2<V, W>> t) throws Exception {
                return t != null;
            }
        });

//        bv.destroy(false);

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
            JavaSparkContext sc,
            I frontierId, // the id of the frontier which to advance
            //Nfa<V, E> nfa,
            JavaPairRDD<V, FrontierData<I, S, V, E>> frontierRdd,
            JavaPairRDD<V, Tuple2<E, V>> rdd,
//            JavaPairRDD<V, Tuple2<E, V>> bwdRdd,
            //boolean reversePropertyDirection,
            boolean isReverseRdd,
            Broadcast<Map<I, Nfa<S, T>>> idToNfa,
            Function<T, PredicateClass> transToPredicateClass // null implies epsilon transition
            //Predicate<T> isEpsilon,
            //java.util.function.Function<T, PredicateClass> transToPredicateClass
            )
     //       Predicate<T> isEpsilon)
    {




        JavaPairRDD<V, FrontierData<I, S, V, E>> result =
            autoJoin(sc, frontierRdd, rdd)

            //.partitionBy(rdd.partitioner().get())
            //.join(rdd, rdd.partitioner().get())
            //.persist(StorageLevel.MEMORY_AND_DISK_SER())
            // TODO Maybe first do map to pair and then filter
            //.join(rdd)
            .filter(new Function<Tuple2<V, Tuple2<FrontierData<I, S, V, E>,Tuple2<E,V>>>, Boolean>() {
                private static final long serialVersionUID = 123513475937L;
                @Override
                public Boolean call(
                        Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>> t)
                                throws Exception {
                    FrontierData<I, S, V, E> frontierData = t._2._1;
                    I nfaId = frontierData.getFrontierId();
                    E p = t._2._2._1;
                    //V o = t._2._2._2;

                    Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);
                    Set<S> states = frontierData.getStates();
                    DirectedGraph<S, T> graph = nfa.getGraph();

                    // Check if any of the transitions accepts the predicate
                    Set<T> transitions = JGraphTUtils.resolveTransitions(graph, trans -> getPredicateClass(transToPredicateClass, trans) == null, states, false);

                    boolean result = transitions.stream().anyMatch(trans -> {
                            PredicateClass pc = getPredicateClass(transToPredicateClass, trans);
                            ValueSet<Node> valueSet = !isReverseRdd ? pc.getFwdNodes() : pc.getBwdNodes();
                            boolean r = valueSet.contains(p);
                            return r;
                    });

                    return result;
                }
            })
            .mapToPair(new PairFunction<Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>>, V, FrontierData<I, S, V, E>>() {
                    //new Function<Tuple2<V, Tuple2<FrontierData<I, S, V, E>,Tuple2<E, V>>>, FrontierItem<I, S, V, E>>() {
                private static final long serialVersionUID = 1312323951L;

                @Override
                public Tuple2<V, FrontierData<I, S, V, E>> call(Tuple2<V, Tuple2<FrontierData<I, S, V, E>, Tuple2<E, V>>> t)
                                throws Exception {

                    FrontierData<I, S, V, E> frontierData = t._2._1;
                    I nfaId = frontierData.getFrontierId();
                    Nfa<S, T> nfa = idToNfa.getValue().get(nfaId);
                    Set<S> states = frontierData.getStates();
                    E p = t._2._2._1;
                    V o = t._2._2._2;


                    DirectedGraph<S, T> graph = nfa.getGraph();

                    // Check if any of the transitions accepts the predicate
                    Set<T> transitions = JGraphTUtils.resolveTransitions(graph, trans -> getPredicateClass(transToPredicateClass, trans) == null, states, false);

                    Set<S> nextStates = transitions.stream()
                            .filter(trans -> {
                                PredicateClass pc = getPredicateClass(transToPredicateClass, trans);
                                ValueSet<Node> valueSet = !isReverseRdd ? pc.getFwdNodes() : pc.getBwdNodes();
                                boolean r = valueSet.contains(p);
                                return r;
                            })
                            .map(trans -> {
                                S r = graph.getEdgeTarget(trans);
                                return r;
                            })
                            .collect(Collectors.toSet());

                    Directed<NestedPath<V, E>> pathHead = frontierData.getPathHead();
                    NestedPath<V, E> nestedPath = frontierData.getPathHead().getValue();

                    NestedPath<V, E> nextPath = new NestedPath<>(new ParentLink<>(nestedPath, new Directed<>(p, false)), o);
                    Directed<NestedPath<V, E>> nextPathHead = new Directed<>(nextPath, pathHead.isReverse());

                    FrontierItem<I, S, V, E> result = new FrontierItem<I, S, V, E>(nfaId, nextStates, nextPathHead);

                    return result;
                }
            })
//            .filter(new Function<Tuple2<V, FrontierData<I,S,V,E>>, Boolean>() {
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                public Boolean call(Tuple2<V, FrontierData<I, S, V, E>> v1)
//                        throws Exception {
//                    return true;
//                }
//            })
//            .partitionBy(rdd.partitioner().get())
            .persist(StorageLevel.MEMORY_AND_DISK_SER());

        // FIXME Interestingly, Sparks execution plan looks ok when using .first() here
        // But why? If .count() is used, the join appears twice in the plan
        //result.first();

        result.count();
        //result.ta

        return result;
    }
}
