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
     *
     *
     *
     * @param nfa
     * @param isEpsilon
     * @return
     */
    public static <S, T> Map<T, Double> estimateFrontierCost(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, PredicateClass> transitionToPredicateClass,
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
            for(S state : current) {
                Set<T> transitions = JGraphTUtils.resolveTransitions(graph, state, isEpsilon);
                Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);

                // For every transition, resolve the property class, and intersect it with the successors of the join summary

                // Once all transitions are iterated, remove all unneeded predicates from the source vertex

                // The set of the source state's referenced predicates
                Set<Node> sourcePredicates = new HashSet<Node>();

                //Map<Node, Map<Node, Number>> targetProdCost = new HashMap<>();



                for(T transition : transitions) {
                    PredicateClass transitionPredClass = transitionToPredicateClass.apply(transition);

                    // for forward and backward direction - 0: forwards, 1: backwards
                    S targetState = graph.getEdgeTarget(transition);
                    Pair<Map<Node, Number>> targetDiPredToCost = stateToDiPredToCost.get(targetState);


                    for(int i = 0; i < 2; ++i) {
                        boolean isChange = false;

                        Map<Node, Number> targetPredToCost = targetDiPredToCost.get(i);
                        Set<Node> targetPreds = targetPredToCost.keySet();

                        boolean reverse = i == 1;

                        //ValueSet<Node> transitionPredicates = pc.get(i);

                        Map<Node, Number> predToCost = diPredToCost.get(i);

                        Set<Node> preds = predToCost.keySet();

                        // for every predicate (that corresponds to the source state), check the join summary for
                        // join candidates
                        Map<Node, Map<Node, Number>> joinSummaryFragment = joinSummaryService.fetch(preds, reverse);

                        for(Entry<Node, Number> predCost : predToCost.entrySet()) {
                            // TODO This join summary lookup is flawed
                            Node pred = predCost.getKey();
                            Number baseCost = predCost.getValue();

                            Map<Node, Number> joinSummary = joinSummaryFragment.get(pred);

                            Map<Node, Number> targetPredCostContrib =
                                    joinSummary.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Entry::getKey,
                                            costEntry -> baseCost.doubleValue() * costEntry.getValue().doubleValue()));

                            Set<Node> contribPreds = targetPredCostContrib.keySet();


                            isChange = !targetPreds.containsAll(contribPreds);
                            if(isChange) {
                                next.add(targetState);
                            }

                            // Compute the costs of the target vertex
                            Map<Node, Number> totalCost = mergeMaps(targetPredToCost, targetPredCostContrib, (a, b) -> a.doubleValue() + b.doubleValue());
                            targetPredToCost.putAll(totalCost);


                            // Compute the set of joinable predicates that matches the transition
                            // joinPreds is the set of predicates that joins with preds
                            Set<Node> joinPreds = joinSummary.keySet();

                            //
                            Set<Node> actualPreds = joinPreds.stream()
                                    .filter(p -> transitionPredClass.contains(p))
                                    .collect(Collectors.toSet());

                            transitionToPreds.putAll(transition, actualPreds);

                            // TODO we may have to iteratively relabel the actual predicates
                            // i.e. if there are cycles, this set gets iteratively refined

                        }


    //
    //                    ValueSet<Node> resolvedPredicates = resolvePredicates(transitionPredicates,)
    //                    if(!transitionPredicates.isEmpty()) {
    //
                        }




                }


            }

            current = next;
            next = new HashSet<>();
        }

        System.out.println(stateToDiPredToCost);


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

            Set<T> transitions = JGraphTUtils.resolveTransitions(graph, states, isEpsilon);

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
