package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EdgeReducer {


    public static <K, V, W> Map<K, Map<V, W>> mergeNestedMap(Map<K, Map<V, W>> a, Map<K, Map<V, W>> b, BinaryOperator<W> mergeFn) {
        Map<K, Map<V, W>> result = mergeMaps(a, b, (x, y) -> mergeMaps(x, y, mergeFn));
        return result;
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> a, Map<K, V> b, BinaryOperator<V> mergeFn) {
        Map<K, V> result = Stream.of(a, b)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        mergeFn));
        return result;
    }

    /**
     * - Vertices: are mapped to the estimated set of predicates with their estimated (maximum) frequency
     * - Edge: Their property classes are resolved to explicit sets of predicates.
     *         Based on the predicates present on their start vertex, an estimate is made
     *
     *
     * The problem is, how to sum up the statistics reaching a vertex from different paths?
     *
     *
     * Duplicates: What if the frontiers reaching a vertex overlap because of duplicates?
     * For example:
     * (a|b)|(a|c)d
     *
     * Maybe this is a problem of rephrasing the automaton
     *
     * TODO We only need to resolve newly added predicates from the join summary
     *
     *
     * @param nfa
     * @param isEpsilon
     * @return
     */
    public static <S, T> Map<T, Double> estimateFrontierCost(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, PredicateClass> transToPredicateClass,
            Pair<Map<Node, Number>> initPredFreqs,
            JoinSummaryService joinSummaryService
            ) {
        // For every transition, we keep track of the possible set of predicates and their estimated frequency (distinct value count)
        // This means, that negative predicate sets are resolved to positive ones via the stateToPredicateFreq map
        Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost = new HashMap<>();

        for(S state : nfa.getGraph().vertexSet()) {
            stateToDiPredToCost.put(state, new Pair<>(new HashMap<Node, Number>(), new HashMap<Node, Number>()));
        }

        for(S state : nfa.getStartStates()) {
            Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);

            for(int i = 0; i < 2; ++i) {
                Map<Node, Number> predToCost = diPredToCost.get(i);

                Map<Node, Number> initPredToCost = initPredFreqs.get(i);

                // TODO Do an in-place merge
                Map<Node, Number> tmp = mergeMaps(predToCost, initPredToCost, (a, b) -> a.doubleValue() + b.doubleValue());
                predToCost.putAll(tmp);
            }
        }


        DirectedGraph<S, T> graph = nfa.getGraph();

        Multimap<T, Node> transitionToPreds = HashMultimap.create();


        Set<S> current = nfa.getStartStates();

        // Maximum number of iterations to perform
        int remainingSteps = 100;

        // The next set of states to consider
        Set<S> next = new HashSet<>();
        while(!current.isEmpty()) {

            Map<S, Pair<Number>> predsInFrontier = current.stream().
                    collect(Collectors.toMap(
                      e -> e,
                      e -> new Pair<Number>(stateToDiPredToCost.get(e).get(0).keySet().size(), stateToDiPredToCost.get(e).get(1).keySet().size())
                    ));

            System.out.println("ENTERING states: " + predsInFrontier);

            for(S srcState : current) {
                //Set<T> transitions = JGraphTUtils.resolveTransitions(graph, state, isEpsilon, false);
                Set<T> transitions = graph.outgoingEdgesOf(srcState);
                Pair<Map<Node, Number>> srcDiPredToCost = stateToDiPredToCost.get(srcState);

                // For every transition, resolve the property class, and intersect it with the successors of the join summary

                // Once all transitions are iterated, remove all unneeded predicates from the source vertex

                // The set of the source state's referenced predicates
//                Set<Node> sourcePredicates = new HashSet<Node>();

                //Map<Node, Map<Node, Number>> targetProdCost = new HashMap<>();



                for(T trans : transitions) {
                    PredicateClass transPredClass = transToPredicateClass.apply(trans);
                    if(isEpsilon.test(trans)) {
                        // If this is an eps transition, just contribute the predicate costs of the source state to the target state

                        S tgtState = graph.getEdgeTarget(trans);
                        Pair<Map<Node, Number>> tgtDiPredToCost = stateToDiPredToCost.get(tgtState);

                        for(int i = 0; i < 2; ++i) {
                            Map<Node, Number> srcPredToCost = srcDiPredToCost.get(0);
                            Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);

                            Map<Node, Number> newTgtPredToCost = mergeMaps(tgtPredToCost, srcPredToCost, (a, b) -> Math.max(a.doubleValue(), b.doubleValue()));

                            boolean isChange = !tgtPredToCost.equals(newTgtPredToCost);
                            if(isChange) {
                                tgtPredToCost.putAll(newTgtPredToCost);
                                next.add(tgtState);
                            }
                        }
                    } else {


                        // for forward and backward direction - 0: forwards, 1: backwards
                        S tgtState = graph.getEdgeTarget(trans);
                        //Set<S> targetStates = JGraphTUtils.transitiveGet(graph, targetState, 1, isEpsilon);


    //                    Set<S> targetStates = JGraphTUtils.resolveTransitions(graph, targetState, isEpsilon, false).stream()
    //                            .map(e -> graph.getEdgeTarget(e)).collect(Collectors.toSet());
                        //System.out.println("Target States: " + targetStates);

                        Pair<Map<Node, Number>> tgtDiPredToCost = stateToDiPredToCost.get(tgtState);



                        for(int i = 0; i < 2; ++i) {
                            boolean isChange = false;

                            Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);
                            Set<Node> tgtPreds = tgtPredToCost.keySet();

                            boolean reverse = i == 1;

                            ValueSet<Node> transPredSet = transPredClass.get(i);

                            Map<Node, Number> srcPredToCost = srcDiPredToCost.get(i);

                            Set<Node> srcPreds = srcPredToCost.keySet();

                            // for every predicate (that corresponds to the source state), check the join summary for
                            // join candidates
                            Map<Node, Map<Node, Number>> joinSummaryFragment = joinSummaryService.fetch(srcPreds, reverse);
                            //Map<Node, Number> joinSummary = joinSummaryService.fetchPredicates(preds, reverse);

                            Map<Node, Number> newTgtPredToCost = new HashMap<>(tgtPredToCost);
                            for(Entry<Node, Number> srcPredCost : srcPredToCost.entrySet()) {
                                // TODO This join summary lookup is flawed
                                Node pred = srcPredCost.getKey();
                                Number baseCost = srcPredCost.getValue();

                                Map<Node, Number> joinSummary = joinSummaryFragment.get(pred);
                                joinSummary = joinSummary == null ? Collections.emptyMap() : joinSummary;

                                Map<Node, Number> tgtPredCostContrib =
                                        joinSummary.entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Entry::getKey,
                                                costEntry -> baseCost.doubleValue() * costEntry.getValue().doubleValue()));

                                Set<Node> tgtContribPreds = tgtPredCostContrib.keySet();


                                isChange = !tgtPreds.containsAll(tgtContribPreds);
                                if(isChange) {
                                    next.add(tgtState);
                                }

                                // Compute the costs of the target vertex
                                Map<Node, Number> totalCost = mergeMaps(newTgtPredToCost, tgtPredCostContrib, (a, b) -> a.doubleValue() + b.doubleValue());
                                newTgtPredToCost.putAll(totalCost);


                                // Compute the set of joinable predicates that matches the transition
                                // joinPreds is the set of predicates that joins with preds
                                Set<Node> joinPreds = joinSummary.keySet();

                                //
                                Set<Node> actualPreds = joinPreds.stream()
                                        .filter(p -> transPredSet.contains(p))
                                        .collect(Collectors.toSet());

                                transitionToPreds.putAll(trans, actualPreds);

                                // TODO we may have to iteratively relabel the actual predicates
                                // i.e. if there are cycles, this set gets iteratively refined

                            }
                            tgtPredToCost.putAll(newTgtPredToCost);
                        }
                    }
                }
            }

            current = next;
            next = new HashSet<>();
        }

        System.out.println(stateToDiPredToCost);


        return null;
    }


    /**
     * This method takes as input an nfa where edges are labeled with the predicates according to the join summary.
     * This method then starts from the target states, and checks which properties join with it.
     * However, predicates that do not join with the predicates are removed. This process is carried out recursively.
     *
     *
     *
     */
    public static <S, T> Map<T, Double> trimPredicates(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, PredicateClass> transitionToPredicateClass,
            Pair<Map<Node, Number>> targetDiPredToCost,
            Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost,
            JoinSummaryService joinSummaryService) {


        Set<S> endStates = nfa.getEndStates();

        for(S state : endStates) {
            Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);
            for(int i = 0; i < 2; ++i) {
                boolean reverse = i == 1;

                Map<Node, Number> predToCost = diPredToCost.get(i);
                Map<Node, Number> targetPredToCost = targetDiPredToCost.get(i);

                // Only retain predicate costs of known predicates
                predToCost.keySet().retainAll(targetPredToCost.keySet());
            }
        }



        // Init: trim the target state to the set of known predicates
        Set<S> current = nfa.getEndStates();
        while(!current.isEmpty()) {
            nfa.getGraph();

            for(S state : current) {
                Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);

                for(int i = 0; i < 2; ++i) {
                    boolean reverse = i == 1;

                    Map<Node, Number> predToCost = diPredToCost.get(i);


                }


                // Cross check the set of predicates that reached the target state with the known set of predicates
                //JGraphTUtils.resolveTransitions(graph, vertex, isEpsilon);
            }

        }

        return null;

    }


    // TODO Create a function that removes all transitions of an nfa
    // that do not contribute to finding paths between source and target

    /**
     * 0: no predicates (occurrs e.g. on accepting states that do not lie on cyclic paths)
     * 1: outbound
     * 2: inbound
     * 3: both
     *
     * @param graph
     * @param state
     * @param toPredicateClass
     * @return
     */
    public static <S, T> int determineRequiredPredicateDirectionsForRetrieval(DirectedGraph<S, T> graph, S state, Function<T, PredicateClass> toPredicateClass) {
        Collection<T> edges = graph.outgoingEdgesOf(state);

        int result = 0;
        for(T edge : edges) {
            PredicateClass pc = toPredicateClass.apply(edge);
            result |= !pc.getFwdNodes().isEmpty() ? 1 : 0;
            result |= !pc.getBwdNodes().isEmpty() ? 2 : 0;
        }

        return result;
    }
/*
    public static <S, T> int mergePredicateClasses(DirectedGraph<S, T> graph, S state, Function<T, PredicateClass> toPredicateClass) {
        Collection<T> edges = graph.outgoingEdgesOf(state);

        int result = 0;
        for(T edge : edges) {
            PredicateClass pc = toPredicateClass.apply(edge);
            result |= !pc.getFwdNodes().isEmpty() ? 1 : 0;
            result |= !pc.getBwdNodes().isEmpty() ? 2 : 0;
        }

        return result;
    }
*/

    // TODO Turn this into a service, that does not have to preload all data into memory
    // TODO Include cardinalities
    public static BiHashMultimap<Node, Node> loadJoinSummary(QueryExecutionFactory qef) {
        BiHashMultimap<Node, Node> result = new BiHashMultimap<>();

        QueryExecution qe = qef.createQueryExecution("PREFIX o: <http://example.org/ontology/> SELECT ?x ?y { ?s o:sourcePredicate ?x ; o:targetPredicate ?y }");
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node x = binding.get(Vars.x);
            Node y = binding.get(Vars.y);

            result.put(x, y);
        }

        return result;
    }


    public static Map<Node, Long> loadPredicateSummary(QueryExecutionFactory qef) {
        Map<Node, Long> result = new HashMap<>();

        QueryExecution qe = qef.createQueryExecution("PREFIX o: <http://example.org/ontology/> SELECT ?x ?y { ?s a o:PredicateSummary ; o:predicate ?x ; o:freqTotal ?y }");
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node x = binding.get(Vars.x);
            Node y = binding.get(Vars.y);

            result.put(x, ((Number)y.getLiteralValue()).longValue());
        }

        return result;
    }





    /**
     * Based on the nfa, a list of all predicates and a predicate join summary,
     * determine the set of predicates referenced by the nfa
     *
     * The set of all predicates is implicitly used by transitionsToPredicates, as this function must resolve negated predicate sets
     *
     * BiHashMultimap<P, P> joinSummary
     *
     * @param nfa
     * @param predicates
     * @param joinSummary
     */
    public static <S, T, P> Set<P> getReferencedPredicates(Nfa<S, T> nfa, Predicate<Entry<P, P>> joins, Predicate<T> isEpsilon, Function<Set<T>, Set<P>> transitionsToPredicates) {
        Set<T> result = new HashSet<>();
        DirectedGraph<S, T> graph = nfa.getGraph();


        boolean change = true;
        Set<S> visited = new HashSet<>();

        Set<S> states = nfa.getStartStates();
        Multimap<S, P> priorTransitions = HashMultimap.create();


        while(change) {
            Set<S> nextStates = new HashSet<S>();
            Multimap<S, P> nextTransitions = HashMultimap.create();

            Set<T> transitions = JGraphTUtils.resolveTransitions(graph, states, isEpsilon, false);

            for(T t : transitions) {
                S state = graph.getEdgeTarget(t);
                Set<P> nextPredicates = transitionsToPredicates.apply(Collections.singleton(t));

                Collection<P> priorPredicates = priorTransitions.get(state);


                Set<P> reachablePredicates = nextPredicates.stream().filter(np ->
                    priorPredicates.stream().anyMatch(p ->
                        joins.test(new SimpleEntry<>(np, p)))
                    ).collect(Collectors.toSet());

                //predicates.stream().forEach(p -> priorTransitions.put(state, p));


            }



            change = visited.addAll(states);

            states = nextStates;



            //Set<P> predicates = transitionsToPredicates.apply(transitions);


        }
        return null;



    }
}
