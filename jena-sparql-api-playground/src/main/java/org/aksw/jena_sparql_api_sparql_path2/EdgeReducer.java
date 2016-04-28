package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class EdgeReducer {


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
//    public static <S, T> Map<T, Double> estimateFrontierCost(
//            Nfa<S, T> nfa,
//            Predicate<T> isEpsilon,
//            Function<T, PredicateClass> transToPredicateClass,
//            Pair<Map<Node, Number>> initPredFreqs,
//            JoinSummaryService joinSummaryService
//            ) {
//        // For every transition, we keep track of the possible set of predicates and their estimated coste (distinct value count)
//        // This means, that negative predicate sets are resolved to positive ones via the stateToPredicateFreq map
//        Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost = new HashMap<>();
//
//
//        for(S state : nfa.getGraph().vertexSet()) {
//            stateToDiPredToCost.put(state, new Pair<>(new HashMap<Node, Number>(), new HashMap<Node, Number>()));
//        }
//
//        // A map for keeping track of the set of predicates that still need to be processed and in which states they appear
//        Pair<BiHashMultimap<Node, S>> openDiPredToStates = new Pair<>(new BiHashMultimap<>(), new BiHashMultimap<>());
//
//
//        //Map<S, Pair<Set<Node>>> openStateToDiPred = new HashMap<>();
//
//        for(S state : nfa.getStartStates()) {
//            Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);
//
//            for(int i = 0; i < 2; ++i) {
//                Map<Node, Number> predToCost = diPredToCost.get(i);
//
//                Map<Node, Number> initPredToCost = initPredFreqs.get(i);
//
//                // TODO Do an in-place merge
//                Map<Node, Number> tmp = mergeMaps(predToCost, initPredToCost, (a, b) -> a.doubleValue() + b.doubleValue());
//                predToCost.putAll(tmp);
//
//                BiHashMultimap<Node, S> openPredToStates = openDiPredToStates.get(i);
//
//                predToCost.keySet().stream().forEach(pred -> openPredToStates.put(pred, state));
//            }
//        }
//
//
//        DirectedGraph<S, T> graph = nfa.getGraph();
//
//        Multimap<T, Node> transitionToPreds = HashMultimap.create();
//
//
//        //Set<S> current = nfa.getStartStates();
//
//        // Maximum number of iterations to perform
//        int remainingSteps = 100;
//
//        // The next set of states to consider
//        Set<S> next = new HashSet<>();
//        //while there are open
//        while(openDiPredToStates.get(0).size() != 0 && openDiPredToStates.get(1).size() != 0) {
//
//            Set<S> current = Sets.union(openDiPredToStates.get(0).getInverse().keySet(), openDiPredToStates.get(1).getInverse().keySet());
//
//            // Count the number of predicates in the frontier for logging purposes
//            Map<S, Pair<Number>> predsInFrontier = current.stream().
//                    collect(Collectors.toMap(
//                      e -> e,
//                      e -> new Pair<Number>(stateToDiPredToCost.get(e).get(0).keySet().size(), stateToDiPredToCost.get(e).get(1).keySet().size())
//                    ));
//
//            System.out.println("ENTERING states: " + predsInFrontier);
//
//            for(int i = 0; i < 2; ++i) {
//                boolean reverse = i == 1;
//                BiHashMultimap<Node, S> openPredToStates = openDiPredToStates.get(i);
//
//                Set<Node> openPreds = openPredToStates.keySet();
//                joinSummaryService.fetch(openPreds, reverse);
//
//
//
//                // Whether to fetch inverse or forward direction depends on the transition
//                Map<Node, Map<Node, Number>> joinSummaryFragment = joinSummaryService.fetch(openPreds, reverse);
//
//                Map<Node, Number> newTgtPredToCost = new HashMap<>(tgtPredToCost);
//                for(Entry<Node, S> openPredState : openPredToStates.entries()) { //.asMap().entrySet()) {
//                    // Get the predicates state
//                    Node pred = openPredState.getKey();
//                    S state = openPredState.getValue();
//                    //Set<S> srcStates = openPredStates.getValue();
//                }
//
//
//
//
//            }
//
//
//
//            for(S srcState : current) {
//                //Set<T> transitions = JGraphTUtils.resolveTransitions(graph, state, isEpsilon, false);
//                Set<T> transitions = graph.outgoingEdgesOf(srcState);
//                Pair<Map<Node, Number>> srcDiPredToCost = stateToDiPredToCost.get(srcState);
//
//                // For every transition, resolve the property class, and intersect it with the successors of the join summary
//
//                // Once all transitions are iterated, remove all unneeded predicates from the source vertex
//
//                // The set of the source state's referenced predicates
////                Set<Node> sourcePredicates = new HashSet<Node>();
//
//                //Map<Node, Map<Node, Number>> targetProdCost = new HashMap<>();
//
//
//
//                for(T trans : transitions) {
//                    PredicateClass transPredClass = transToPredicateClass.apply(trans);
//                    if(isEpsilon.test(trans)) {
//                        // If this is an eps transition, just contribute the predicate costs of the source state to the target state
//
//                        S tgtState = graph.getEdgeTarget(trans);
//                        Pair<Map<Node, Number>> tgtDiPredToCost = stateToDiPredToCost.get(tgtState);
//
//                        for(int i = 0; i < 2; ++i) {
//
//                            Map<Node, Number> srcPredToCost = srcDiPredToCost.get(i);
//                            Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);
//
//                            Map<Node, Number> newTgtPredToCost = mergeMaps(tgtPredToCost, srcPredToCost, (a, b) -> Math.max(a.doubleValue(), b.doubleValue()));
//
//                            boolean isChange = !tgtPredToCost.equals(newTgtPredToCost);
//                            if(isChange) {
//                                tgtPredToCost.putAll(newTgtPredToCost);
//                                next.add(tgtState);
//                            }
//                        }
//                    } else {
//
//
//                        // for forward and backward direction - 0: forwards, 1: backwards
//                        S tgtState = graph.getEdgeTarget(trans);
//                        //Set<S> targetStates = JGraphTUtils.transitiveGet(graph, targetState, 1, isEpsilon);
//
//
//    //                    Set<S> targetStates = JGraphTUtils.resolveTransitions(graph, targetState, isEpsilon, false).stream()
//    //                            .map(e -> graph.getEdgeTarget(e)).collect(Collectors.toSet());
//                        //System.out.println("Target States: " + targetStates);
//
//                        Pair<Map<Node, Number>> tgtDiPredToCost = stateToDiPredToCost.get(tgtState);
//
//
//
//                        for(int i = 0; i < 2; ++i) {
//                            boolean isChange = false;
//
//                            Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);
//                            Set<Node> tgtPreds = tgtPredToCost.keySet();
//
//                            boolean reverse = i == 1;
//
//                            ValueSet<Node> transPredSet = transPredClass.get(i);
//
//                            if(transPredSet.isEmpty()) {
//                                continue;
//                            }
//
//                            Map<Node, Number> srcPredToCost = srcDiPredToCost.get(i);
//
//                            Set<Node> srcPreds = srcPredToCost.keySet();
//
//                            // for every predicate (that corresponds to the source state), check the join summary for
//                            // join candidates
//                            Map<Node, Map<Node, Number>> joinSummaryFragment = joinSummaryService.fetch(srcPreds, reverse);
//                            //Map<Node, Number> joinSummary = joinSummaryService.fetchPredicates(preds, reverse);
//
//                            Map<Node, Number> newTgtPredToCost = new HashMap<>(tgtPredToCost);
//                            for(Entry<Node, Number> srcPredCost : srcPredToCost.entrySet()) {
//                                // TODO This join summary lookup is flawed
//                                Node pred = srcPredCost.getKey();
//                                Number baseCost = srcPredCost.getValue();
//
//                                Map<Node, Number> joinSummary = joinSummaryFragment.get(pred);
//                                joinSummary = joinSummary == null ? Collections.emptyMap() : joinSummary;
//
//                                Map<Node, Number> tgtPredCostContrib =
//                                        joinSummary.entrySet().stream()
//                                        .collect(Collectors.toMap(
//                                                Entry::getKey,
//                                                costEntry -> baseCost.doubleValue() * costEntry.getValue().doubleValue()));
//
//                                Set<Node> tgtContribPreds = tgtPredCostContrib.keySet();
//
//
//                                isChange = !tgtPreds.containsAll(tgtContribPreds);
//                                if(isChange) {
//                                    next.add(tgtState);
//                                }
//
//                                // Compute the costs of the target vertex
//                                Map<Node, Number> totalCost = mergeMaps(newTgtPredToCost, tgtPredCostContrib, (a, b) -> a.doubleValue() + b.doubleValue());
//                                newTgtPredToCost.putAll(totalCost);
//
//
//                                // Compute the set of joinable predicates that matches the transition
//                                // joinPreds is the set of predicates that joins with preds
//                                Set<Node> joinPreds = joinSummary.keySet();
//
//                                //
//                                Set<Node> actualPreds = joinPreds.stream()
//                                        .filter(p -> transPredSet.contains(p))
//                                        .collect(Collectors.toSet());
//
//                                transitionToPreds.putAll(trans, actualPreds);
//
//                                // TODO we may have to iteratively relabel the actual predicates
//                                // i.e. if there are cycles, this set gets iteratively refined
//
//                            }
//                            tgtPredToCost.putAll(newTgtPredToCost);
//                        }
//                    }
//                }
//            }
//
//            current = next;
//            next = new HashSet<>();
//        }
//
//        stateToDiPredToCost.entrySet().stream().forEach(e -> System.out.println(e));
//
//
//        return null;
//    }



//    public static <S, T, V, E> Graph<V, E> createJoinSummaryExcerpt(
//            Nfa<S, T> nfa,
//            Predicate<T> isEpsilon,
//            Function<T, PredicateClass> transToPredicateClass,
//            Pair<Map<Node, Number>> initPredFreqs,
//            JoinSummaryService joinSummaryService) {
//
//        // Map to keep track which predicates were reached in the state in which iteration
//        // The iteration corresponds to the minimum path length that may reach the predicate
//        Map<S, NavigableMap<Integer, Pair<Map<Node, Number>>>> stateToIterToDiPredToCost = new HashMap<>();
//
//        for(S state : nfa.getGraph().vertexSet()) {
//            stateToIterToDiPredToCost.put(state, new TreeMap<>());
//        }
//
//        int iter = 0;
//
//        // Initiale the zero'th iteration
//        for(S state : nfa.getStartStates()) {
//            NavigableMap<Integer, Pair<Map<Node, Number>>> iterToDiPredToCost = stateToIterToDiPredToCost.get(state);
//            iterToDiPredToCost.get(iter);
//
//        }
//
//
//
//        DirectedGraph<Integer, DefaultEdge> joinGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
//
//
//
//
//
//    }

    //Map<S, Pair<Map<Node, Number>>>
    public static <S, T> NfaAnalysisResult<S> estimateFrontierCost(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, PredicateClass> transToPredicateClass,
            Pair<Map<Node, Number>> initPredFreqs,
            JoinSummaryService joinSummaryService
            ) {
        // For every transition, we keep track of the possible set of predicates and their estimated frequency (distinct value count)
        // This means, that negative predicate sets are resolved to positive ones via the stateToPredicateFreq map
        Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost = new HashMap<>();


        // The set of predicates for which the join
        Pair<Set<Node>> currOpenDiPreds = new Pair<>(new HashSet<>(), new HashSet<>());

        for(S state : nfa.getGraph().vertexSet()) {
            stateToDiPredToCost.put(state, new Pair<>(new HashMap<Node, Number>(), new HashMap<Node, Number>()));
        }

        // A map for keeping track of the set of predicates that still need to be processed and in which states they appear
        //Pair<BiHashMultimap<Node, S>> openDiPredToStates = new Pair<>(new BiHashMultimap<>(), new BiHashMultimap<>());
        //Pair</Se>

        // Excerpt of the join summary used for reachability
        DirectedGraph<Node, DefaultEdge> joinGraph = new DefaultDirectedGraph<>(DefaultEdge.class);



        //Map<S, Pair<Set<Node>>> openStateToDiPred = new HashMap<>();

        for(S state : nfa.getStartStates()) {
            Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(state);

            for(int i = 0; i < 2; ++i) {
                Map<Node, Number> predToCost = diPredToCost.get(i);

                Map<Node, Number> initPredToCost = initPredFreqs.get(i);

                //Map<Node, Number> tmp =
                MapUtils.mergeMapsInPlace(predToCost, initPredToCost, (a, b) -> a.doubleValue() + b.doubleValue());
                //predToCost.putAll(tmp);

                //BiHashMultimap<Node, S> openPredToStates = openDiPredToStates.get(i);
                Set<Node> openPreds = currOpenDiPreds.get(i);
                openPreds.addAll(predToCost.keySet());

                //predToCost.keySet().stream().forEach(pred -> openPredToStates.put(pred, state));
            }
        }


        DirectedGraph<S, T> graph = nfa.getGraph();

        //Multimap<T, Node> transitionToPreds = HashMultimap.create();


        Set<S> currOpenStates = nfa.getStartStates();

        // Maximum number of iterations to perform
        int remainingSteps = 100;

        // The next set of states to consider
        //while(!current.isEmpty() && (remainingSteps--) > 0) {
        //while there are open
        while((currOpenDiPreds.get(0).size() != 0 || currOpenDiPreds.get(1).size() != 0) && remainingSteps-- > 0) {

            Set<S> nextOpenStates = new HashSet<>();
            Pair<Set<Node>> nextOpenDiPreds = new Pair<>(new HashSet<>(), new HashSet<>());

            //Set<S> current = Sets.union(openDiPredToStates.get(0).getInverse().keySet(), openDiPredToStates.get(1).getInverse().keySet());

            // Count the number of predicates in the frontier for logging purposes
            Map<S, Pair<Number>> predsInFrontier = currOpenStates.stream().
                    collect(Collectors.toMap(
                      e -> e,
                      e -> new Pair<Number>(stateToDiPredToCost.get(e).get(0).keySet().size(), stateToDiPredToCost.get(e).get(1).keySet().size())
                    ));

            System.out.println("ENTERING states: " + predsInFrontier);



            for(S srcState : currOpenStates) {
                //Set<T> transitions = JGraphTUtils.resolveTransitions(graph, state, isEpsilon, false);
                Set<T> transitions = graph.outgoingEdgesOf(srcState);
                Pair<Map<Node, Number>> srcDiPredToCost = stateToDiPredToCost.get(srcState);

                // For every transition, resolve the property class, and intersect it with the successors of the join summary
                // Once all transitions are iterated, remove all unneeded predicates from the source vertex


                for(T trans : transitions) {
                    S tgtState = graph.getEdgeTarget(trans);
                    Pair<Map<Node, Number>> tgtDiPredToCost = stateToDiPredToCost.get(tgtState);

                    nextOpenStates.add(tgtState);

                    if(isEpsilon.test(trans)) {
                        // If this is an eps transition, just contribute the predicate costs of the source state to the target state
                        for(int i = 0; i < 2; ++i) {

                            Set<Node> nextOpenPreds = nextOpenDiPreds.get(i);

                            Map<Node, Number> srcPredToCost = srcDiPredToCost.get(i);
                            Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);

                            // Contribute all predicates to the transition's target vertex
                            // that are not yet associated with the target
                            Set<Node> srcPreds = srcPredToCost.keySet();
                            Set<Node> tgtPreds = tgtPredToCost.keySet();

                            Set<Node> tgtContribPreds = srcPreds.stream()
                                    .filter(p -> !tgtPreds.contains(p))
                                    .collect(Collectors.toSet());

                            MapUtils.mergeMapsInPlace(tgtPredToCost, srcPredToCost, (a, b) -> Math.max(a.doubleValue(), b.doubleValue()));

                            nextOpenPreds.addAll(tgtContribPreds);
                        }
                    } else {
                        PredicateClass transPredClass = transToPredicateClass.apply(trans);

                        // Determine whether the transitition's predicate class is makes use of predicates that are outbound, inbound or both
                        //transPredClass.get(0).isEmpty()

                        for(int i = 0; i < 2; ++i) {
                            boolean reverse = i == 1;

                            ValueSet<Node> transPredSet = transPredClass.get(i);

                            Set<Node> currOpenPreds = currOpenDiPreds.get(i);
                            Set<Node> nextOpenPreds = nextOpenDiPreds.get(i);

                            // If the predicate class is empty for a direction, there is nothing to do for this direction
                            if(!transPredSet.isEmpty()) {

                                Map<Node, Number> srcPredToCost = srcDiPredToCost.get(i);
                                Map<Node, Number> tgtPredToCost = tgtDiPredToCost.get(i);
                                Set<Node> tgtPreds = tgtPredToCost.keySet();


                                // srcPredToCost may be null if the predicates are not known - this happens if there is a direction change in the predicates
                                // In this case, we can contribute the set of all predicates that join with those matched by the transitions' predicate class
                                if(srcPredToCost == null) {
                                    // resolve the predicate class to the set of predicates

                                    throw new RuntimeException("not implemented yet");
                                }

                                // Get the source predicates, and filter them by the current transitions' predicate class
                                //Set<Node> srcPreds = srcPredToCost.keySet();
                                //Set<Node> srcPassPreds = srcPreds.stream().filter(p -> transPredSet.contains(p)).collect(Collectors.toSet());

                                Set<Node> openPassPreds = currOpenPreds.stream()
                                        .filter(p -> transPredSet.contains(p))
                                        .collect(Collectors.toSet());

                                // Now, for the predicates that passed through the transition,
                                // label the target vertex with the potentially joining predicates of the join summary
                                Map<Node, Map<Node, Number>> joinSummaryFragment = joinSummaryService.fetch(openPassPreds, reverse);

                                if(joinSummaryFragment == null) {
                                    throw new RuntimeException("Join summary fragment was null - should not happen");
                                }

                                //Map<Node, Number> newTgtPredToCost = new HashMap<>(tgtPredToCost);
                                for(Node openPassPred : openPassPreds) {
                                    //Number baseCost = srcPredToCost.get(openPassPred);
                                    Number baseCost = srcPredToCost.getOrDefault(openPassPred, 0);

                                    if(baseCost == null) {
                                        throw new RuntimeException("No base cost for " + openPassPred + " - should not happen");
                                    }

                                    Map<Node, Number> joinPredToCost = joinSummaryFragment.getOrDefault(openPassPred, Collections.emptyMap());
                                    Set<Node> joinPreds = joinPredToCost.keySet();

                                    // find out, which of the joining predicates are newly contributed
                                    Set<Node> tgtContribPreds = joinPreds.stream()
                                            .filter(p -> !tgtPreds.contains(p))
                                            .collect(Collectors.toSet());


                                    // for those, compute the cost

                                    Map<Node, Number> tgtPredCostContrib =
                                            tgtContribPreds.stream()
                                            .map(p -> new SimpleEntry<>(p, baseCost.doubleValue() * joinPredToCost.get(p).doubleValue()))
                                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


                                    // Compute the costs of the target vertex
                                    MapUtils.mergeMapsInPlace(tgtPredToCost, tgtPredCostContrib, (a, b) -> a.doubleValue() + b.doubleValue());
//                                    Map<Node, Number> totalCost =
//                                    tgtPredToCost.putAll(totalCost);

                                    // Update the join graph
                                    joinGraph.addVertex(openPassPred);
                                    tgtContribPreds.stream().forEach(tgtPred -> {
                                        joinGraph.addVertex(tgtPred);
                                        joinGraph.addEdge(openPassPred, tgtPred);
                                    });

                                    nextOpenPreds.addAll(tgtContribPreds);
                                    // Compute the set of joinable predicates that matches the transition
                                    // joinPreds is the set of predicates that joins with preds
                                    //Set<Node> joinPreds = joinPredToCost.keySet();

                                    //
//                                    Set<Node> actualPreds = joinPreds.stream()
//                                            .filter(p -> transPredSet.contains(p))
//                                            .collect(Collectors.toSet());
//
//                                    transitionToPreds.putAll(trans, actualPreds);

                                    // TODO we may have to iteratively relabel the actual predicates
                                    // i.e. if there are cycles, this set gets iteratively refined

                                }
                                System.out.println("Join graph size now: " + joinGraph.edgeSet().size() + " after " + openPassPreds.size() + " predicates passing transition " + trans);
                                //tgtPredToCost.putAll(newTgtPredToCost);
                            }

                        }
                    }
                }
            }

            currOpenStates = nextOpenStates;
            currOpenDiPreds = nextOpenDiPreds;
        }

//        System.out.println("COSTS:");
//        stateToDiPredToCost.entrySet().stream().forEach(e -> System.out.println(e));


        return new NfaAnalysisResult<>(stateToDiPredToCost, joinGraph);
        //return stateToDiPredToCost;
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
            Pair<Map<Node, Number>> initDiPredToCost,
            Map<S, Pair<Map<Node, Number>>> stateToDiPredToCost,
            JoinSummaryService joinSummaryService) {

        DirectedGraph<S, T> graph = nfa.getGraph();


        // The set of remaining predicates per state
        Map<S, Pair<Set<Node>>> stateToDiRestPreds = new HashMap<>();//new Pair<>(HashMultimap.create(), HashMultimap.create());;
        for(S state : nfa.getGraph().vertexSet()) {
            stateToDiRestPreds.put(state, new Pair<>(new HashSet<>(), new HashSet<>()));
        }


        Set<S> endStates = nfa.getEndStates();


        for(S endState : endStates) {
            Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(endState);
            Pair<Set<Node>> diRestPreds = stateToDiRestPreds.get(endState);

            for(int i = 0; i < 2; ++i) {
                boolean reverse = i == 1;

                Map<Node, Number> predToCost = diPredToCost.get(i);
                Map<Node, Number> initPredToCost = initDiPredToCost.get(i);

//                Set<Node> restPreds = diRestPreds.get(i);

                Set<Node> preds = predToCost.keySet();
                Set<Node> initPreds = initPredToCost.keySet();

                preds.retainAll(initPreds);
            }
        }



        // Init: trim the target state to the set of known predicates
        Set<S> currOpenStates = nfa.getEndStates();
        while(!currOpenStates.isEmpty()) {


            for(S tgtState : currOpenStates) {
                Set<T> transitions = graph.incomingEdgesOf(tgtState);

                for(T trans : transitions) {



                }

                Pair<Map<Node, Number>> diPredToCost = stateToDiPredToCost.get(tgtState);

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

            Set<T> transitions = JGraphTUtils.resolveTransitions(graph, isEpsilon, states, false);

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
