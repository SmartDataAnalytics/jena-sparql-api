package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.collections.MultiMaps;
import org.aksw.jena_sparql_api.sparql_path2.JGraphTUtils;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.NfaExecutionUtils;
import org.aksw.jena_sparql_api.sparql_path2.ValueSet;
import org.aksw.jena_sparql_api.sparql_path2.VertexClass;
import org.aksw.jena_sparql_api.utils.Pair;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletImpl;
import org.aksw.jena_sparql_api.utils.model.TripletPath;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


class DijkstraContext {

}



/**
 * Just a scratch pad for my thoughts whether dijkstra could be applied to nfa matching.
 *
 * classic dijstra tracks
 * cost(v)
 * predecessor(v) - vertex from which the minimal cost originated
 *
 *
 * we could track the minimum cost per each predecessor state
 *
 * vertex B was reached in state Y by predecessor A in this state X
 *
 * Essentially this is dijkstra on a graph that is dynamically generated.
 * The vertices are (state, node) pairs, wherease for edges it is (transition, triple) pairs (transition is a triplet<S, PredicateClass>)
 * %Note, that it is essential for the edge also to include the transition, because
 *
 * Given the RPQ: <p>/<p>/<p> - should a p a match? if not, we do not have to track the automaton transitions.
 *
 * An edge in the
 *
 *
 *
 *
 *
 * @author raveni
 *
 */
public class NfaDijkstra {

    /**
     * Check whether a triplet originated from a certain transition
     *
     * @param transition
     * @param transToVertexClass
     * @param vertex
     * @param triplet
     * @return
     */
//    public static <T, V, E> boolean isOrigin(Pair<ValueSet<V>> vertexClass, Triplet<V, Directed<E>> triplet) {
//        //ValueSet<V> valueSet
//    }

    public static <T, V, E> boolean isOrigin(Pair<ValueSet<V>> vertexClass, V vertex, Triplet<V, E> triplet) { //T transition, Function<T, Pair<ValueSet<V>>> transToVertexClass,
        List<Boolean> dirs = TripletImpl.getDirections(triplet, vertex);
        //Pair<ValueSet<V>> vertexClass = transToVertexClass.apply(transition);

        boolean result = dirs.stream().anyMatch(dir -> {
            int index = dir == false ? 0 : 1;
            ValueSet<V> valueSet = vertexClass.get(index);

            E p = triplet.getPredicate();
            boolean r = valueSet.contains(p);
            return r;
        });

        return result;
    }


//    public static void main(String[] args) {
//        QueryExecutionFactory qef = FluentQueryExecutionFactory.model(
//                RDFDataMgr.loadModel("")).create();
//
//        Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node,Node>>>> fn =
//                pc -> PathExecutionUtils.createLookupService(qef, pc);
//
//        Nfa<Integer, PredicateClass> nfa;
//
//        run(nfa, t -> t.)
//
//    }


    public static <V, E> TripletPath<V, Directed<E>> dijkstra(
            Function<? super Iterable<V>, Map<V, Set<Triplet<V, Directed<E>>>>> successors,
            Collection<V> sources,
            Predicate<V> isTarget)
    {

        // We assign a cost to the vertex, but we not only need to track the preceeding vertex but also which edge was used
        Map<V, Integer> vertexToCost = new HashMap<>();
        Map<V, Triplet<V, Directed<E>>> vertexToMinCostPredecessor = new HashMap<>();


        //Multimap<S, V> open = HashMultimap.create();

        //TreeMultimap<Integer, Entry<S, V>> costToVertices = TreeMultimap.create();

        //TreeMap<Integer, Multimap<S, V>> costToStateToVertices = new TreeMap<>();
        //PriorityQueue<Entry<Integer, Entry<S, V>>> queue = new PriorityQueue<>((a, b) -> b.getKey() - a.getKey());


        Set<V> open = new HashSet<>();
        Set<V> seen = new HashSet<>();


        //Multimap<Entry<S, V>, Triplet<Entry<S, V>, E>> result = HashMultimap.create();

        //Frontier<Entry<S, V>> frontier = new FrontierImpl<>();
        for(V source : sources) {
            vertexToCost.put(source, 0);
        }

        open.addAll(sources);
                //open.put(e.getKey(), e.getValue());
                //frontier.add(e);
//                vertexToCost.put(e, 0);
//                Multimap<S, V> mm = costToStateToVertices.get(0);
//                if(mm == null) {
//                    mm = HashMultimap.create();
//                    costToStateToVertices.put(0, mm);
//                }
//                mm.put(s, source);
//                queue.add(new SimpleEntry<>(0, e));


        //stateVertexPairs.forEach(e -> open.put(e.getKey(), e.getValue()));

        V reachedTargetVertex = null;

        while(!open.isEmpty()) { // TODO Pick vertex with lowest cost first
            seen.addAll(open);
            //queue.poll()
            // If a target vertex was reached, skip all vertices whose cost is already higher
            //Multimap<S, V> next = HashMultimap.create();
            Set<V> next = new HashSet<>();



            //Entry<Integer, Entry<S, V>> x = queue.poll();


            //int baseCost = x.getKey();
            //Entry<S, V> currentVertex = x.getValue();
            //int baseCost = vertexToCost.get(currentVertex);
            //int thisCost = baseCost + 1;

            // Fetch the successors
//            Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> succs = getSuccessors(
//                    nfa,
//                    isEpsilon,
//                    transToVertexClass,
//                    createTripletLookupService,
//                    open);
            Map<V, Set<Triplet<V, Directed<E>>>> succs = successors.apply(open);

            // For every successor check whether it can now be reached with a lower cost than before
            for(Entry<V, Set<Triplet<V, Directed<E>>>> entry : succs.entrySet()) {

                V v = entry.getKey();
                int baseCost = vertexToCost.get(v);
                int thisCost = baseCost + 1;

                for(Triplet<V, Directed<E>> triplet : entry.getValue()) {


                    //Triplet<Entry<S, V>, Directed<E>> triplet = entry.getValue();

                    V succ = triplet.getObject();

                    //next.put(succ.getKey(), succ.getValue());
                    next.add(succ);

                    //S state = succ.getKey();
                    //V vertex = succ.getValue();

                    Integer targetMinCost = vertexToCost.getOrDefault(succ, null);

                    //int targetMinCost = _targetMinCost.intValue();

                    if(targetMinCost == null || thisCost < targetMinCost) {
                        vertexToCost.put(succ, thisCost);
                        vertexToMinCostPredecessor.put(succ, triplet); //entry.getValue()));

                        //boolean isAcceptingState = NfaExecutionUtils.isFinalState(nfa, state, isEpsilon);//nfa.getEndStates().contains(state);
                        //boolean isTargetVertex = target.equals(vertex);

                        //if(isAcceptingState && isTargetVertex) {
                        boolean isTargetVertex = isTarget.test(succ);
                        if(isTargetVertex) {
                            reachedTargetVertex = succ;
                        }
                    }
                }

            }
            //int alt
            next.removeAll(seen);
            open = next;

        }

        System.out.println("Backtracking for: " + reachedTargetVertex);
        // Create the path by backtracking from the reached target vertex
        TripletPath<V, Directed<E>> result;

        if(reachedTargetVertex != null) {
            List<Triplet<V, Directed<E>>> path = new ArrayList<>();
            V o = reachedTargetVertex;
            V end = o;
            while(o != null) {
                //V start = eo.getValue();
                // TODO Check whether we reached the start state
                //S state = eo.getKey();

                //boolean isStartState = NfaExecutionUtils.isStartState(nfa, state, isEpsilon);
                //boolean isStartVertex = source.equals(start);
                if(sources.contains(o)) {
                    break;
                }
//                if(isStartVertex && isStartState) {
//                    break;
//                }


                Triplet<V, Directed<E>> predecessor = vertexToMinCostPredecessor.get(o);
                if(predecessor == null) {
                    System.out.println("should not happen");
                }
                V s = predecessor.getSubject();
                //V s = es.getValue();
                Directed<E> dp = predecessor.getPredicate();
                //V o = eo.getValue();

                path.add(new TripletImpl<V, Directed<E>>(s, dp, o));
                o = s;
            }

            Collections.reverse(path);

            result = new TripletPath<>(o, end, path);
        } else {
            result = null;
        }

        return result;

    }


//    public static TripletPath<V, Directed<E>> dijkstra(
//            Function<? super Iterable<V>, Map<V, Set<Triplet<V, Directed<E>>>>> successors,
//            Collection<V> sources, Predicate<V> isTarget) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     * Finds the shortest path connecting the source and target nodes
     * in accordance with an nfa
     *
     * @param nfa
     * @param isEpsilon
     * @param transToVertexClass
     * @param createTripletLookupService
     * @param source
     * @param target
     */
    public static <S, T, V, E> TripletPath<V, E> dijkstra(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, Pair<ValueSet<V>>> transToVertexClass,
            Function<Pair<ValueSet<V>>, Function<? super Iterable<V>, Map<V, Set<Triplet<V, E>>>>> createTripletLookupService,
            //Function<Pair<ValueSet<V>>, Function<? super Iterable<V>, Set<Triplet<V,E>>>> createTripletLookupService,
            V source,
            V target) {

        //Set<S> endStates = NfaExecutionUtils.

        // We assign a cost to the vertex, but we not only need to track the preceeding vertex but also which edge was used
        Map<Entry<S, V>, Integer> vertexToCost = new HashMap<>();
        Map<Entry<S, V>, Triplet<Entry<S, V>, E>> vertexToMinCostPredecessor = new HashMap<>();


        //Multimap<S, V> open = HashMultimap.create();

        //TreeMultimap<Integer, Entry<S, V>> costToVertices = TreeMultimap.create();

        //TreeMap<Integer, Multimap<S, V>> costToStateToVertices = new TreeMap<>();
        //PriorityQueue<Entry<Integer, Entry<S, V>>> queue = new PriorityQueue<>((a, b) -> b.getKey() - a.getKey());


        Set<Entry<S, V>> open = new HashSet<>();
        Set<Entry<S, V>> seen = new HashSet<>();


        //Multimap<Entry<S, V>, Triplet<Entry<S, V>, E>> result = HashMultimap.create();

        //Frontier<Entry<S, V>> frontier = new FrontierImpl<>();
        for(S s : nfa.getStartStates()) {
            Entry<S, V> e = new SimpleEntry<>(s, source);
            open.add(e);
            vertexToCost.put(e, 0);
        }
                //open.put(e.getKey(), e.getValue());
                //frontier.add(e);
//                vertexToCost.put(e, 0);
//                Multimap<S, V> mm = costToStateToVertices.get(0);
//                if(mm == null) {
//                    mm = HashMultimap.create();
//                    costToStateToVertices.put(0, mm);
//                }
//                mm.put(s, source);
//                queue.add(new SimpleEntry<>(0, e));


        //stateVertexPairs.forEach(e -> open.put(e.getKey(), e.getValue()));

        Entry<S, V> reachedTargetVertex = null;

        while(!open.isEmpty()) { // TODO Pick vertex with lowest cost first
            seen.addAll(open);
            //queue.poll()
            // If a target vertex was reached, skip all vertices whose cost is already higher
            //Multimap<S, V> next = HashMultimap.create();
            Set<Entry<S, V>> next = new HashSet<>();



            //Entry<Integer, Entry<S, V>> x = queue.poll();


            //int baseCost = x.getKey();
            //Entry<S, V> currentVertex = x.getValue();
            //int baseCost = vertexToCost.get(currentVertex);
            //int thisCost = baseCost + 1;

            // Fetch the successors
            Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> succs = getSuccessors(
                    nfa,
                    isEpsilon,
                    transToVertexClass,
                    createTripletLookupService,
                    open);

            // For every successor check whether it can now be reached with a lower cost than before
            for(Entry<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> entry : succs.entrySet()) {

                Entry<S, V> v = entry.getKey();
                int baseCost = vertexToCost.get(v);
                int thisCost = baseCost + 1;

                for(Triplet<Entry<S, V>, Directed<E>> triplet : entry.getValue()) {



                    //Triplet<Entry<S, V>, Directed<E>> triplet = entry.getValue();

                    Entry<S, V> succ = triplet.getObject();

                    //next.put(succ.getKey(), succ.getValue());
                    next.add(succ);

                    S state = succ.getKey();
                    V vertex = succ.getValue();

                    Integer targetMinCost = vertexToCost.getOrDefault(succ, null);

                    //int targetMinCost = _targetMinCost.intValue();

                    if(targetMinCost == null || thisCost < targetMinCost) {
                        vertexToCost.put(succ, thisCost);
                        vertexToMinCostPredecessor.put(succ, TripletImpl.makeUndirected(triplet)); //entry.getValue()));

                        boolean isAcceptingState = NfaExecutionUtils.isFinalState(nfa, state, isEpsilon);//nfa.getEndStates().contains(state);
                        boolean isTargetVertex = target.equals(vertex);

                        if(isAcceptingState && isTargetVertex) {
                            reachedTargetVertex = succ;
                        }
                    }
                }

            }
            //int alt
            next.removeAll(seen);
            open = next;

        }

        System.out.println("Backtracking for: " + reachedTargetVertex);
        // Create the path by backtracking from the reached target vertex
        TripletPath<V, E> result;

        if(reachedTargetVertex != null) {
            List<Triplet<V, E>> path = new ArrayList<>();
            Entry<S, V> eo = reachedTargetVertex;
            while(eo != null) {
                V start = eo.getValue();
                // TODO Check whether we reached the start state
                S state = eo.getKey();

                boolean isStartState = NfaExecutionUtils.isStartState(nfa, state, isEpsilon);
                boolean isStartVertex = source.equals(start);
                if(isStartVertex && isStartState) {
                    break;
                }

                Triplet<Entry<S, V>, E> predecessor = vertexToMinCostPredecessor.get(eo);
                if(predecessor == null) {
                    System.out.println("should not happen");
                }
                Entry<S, V> es = predecessor.getSubject();
                V s = es.getValue();
                E p = predecessor.getPredicate();
                V o = eo.getValue();

                path.add(new TripletImpl<V, E>(s, p, o));
                eo = es;
            }

            Collections.reverse(path);

            result = new TripletPath<>(source, target, path);
        } else {
            result = null;
        }

        return result;
    }


    /**
     *
     * Note:
     * When when requesting successors of a vertex, the state component will be resolved to non-epsilon state,
     * and the result will be accociated back with the requested state
     *
     *
     * Note:
     * we could require T to be T extends Pair<ValueSet<V>>
     *
     *
     * @param nfa
     * @param isEpsilon
     * @param transToVertexClass Mapping from nfa transitions to vertex classes
     * @param createTripletLookupService
     * @param stateVertexPairs
     * @return
     */
    public static <S, T, V, E>  Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> getSuccessors(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass,
            Function<Pair<ValueSet<V>>, ? extends Function<? super Iterable<V>, Map<V, Set<Triplet<V, E>>>>> createTripletLookupService,
            Iterable<Entry<S, V>> stateVertexPairs
            )
    {
        // A graph for the data loaded so far
        //DirectedGraph<Entry<S, V>, Triplet<Entry<S, V>, E>> dynamicGraph = new DefaultDirectedGraph<>(Triplet.class);

        //Map<? super Entry<S, V>, Number> vertexToCost = new HashMap<>(); //Comparable<Number>

        //Set<Entry<S, V>> open = new HashSet<>();
        Multimap<S, V> open = HashMultimap.create();
        //Multimap<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>> result = HashMultimap.create();
        Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>> result = new HashMap<>();

        stateVertexPairs.forEach(e -> open.put(e.getKey(), e.getValue()));

        //fa.getStartStates().forEach(s -> open.put(s, start));
//        Frontier<Entry<S, V>> open = new FrontierImpl<>();
//        stateVertexPairs.forEach(e -> open.add(e));


        DirectedGraph<S, T> nfaGraph = nfa.getGraph();
//        while(!open.isEmpty()) {
        //open.asMap().entrySet().forEach(e -> {
        for(Entry<S, Collection<V>> e : open.asMap().entrySet()) {
            // Get all successor states and
            S state = e.getKey();
            Collection<V> vertices = e.getValue();

            Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, isEpsilon, state, false);
            //Set<T> transitions = nfaGraph.outgoingEdgesOf(state);

            // TODO Factor out the part that fetches relations according to the nfa

            Pair<ValueSet<V>> vertexClass = transitions.stream().reduce(
                    (Pair<ValueSet<V>>)new VertexClass<V>(),
                    (a, b) -> transToVertexClass.apply(b),
                    (a, b) -> VertexClass.union(a, b));


            //LookupService<V, Set<Triplet<V, E>>> tripletService = createTripletLookupService.apply(vertexClass);
            Function<? super Iterable<V>, Map<V, Set<Triplet<V, E>>>> tripletService = createTripletLookupService.apply(vertexClass);

            // For all nodes in the state
            Map<V, Set<Triplet<V, E>>> vToTriplets = tripletService.apply(vertices); //, 1);

            // We now need to check which triplet matched which transition so that we associate the right successor state
            vToTriplets.entrySet().forEach(vToTriplet -> {
                V v = vToTriplet.getKey();
                Entry<S, V> source = new SimpleEntry<>(state, v);

                // for every individual transition
                transitions.stream().forEach(t -> {
                    Pair<ValueSet<V>> vc = transToVertexClass.apply(t);

                    // for every triplet check for its origin
                    vToTriplet.getValue().stream().forEach(rawTriplet -> {
                        Triplet<V, Directed<E>> triplet = TripletImpl.makeDirected(rawTriplet, v);
                        V targetVertex = triplet.getObject();
                        //V targetVertex = Triplet.getTarget(triplet, v);
                        //V targetVertex = Triplet.getTarget(triplet, reverse);
                        boolean isOrig = isOrigin(vc, v, rawTriplet);
                        if(isOrig) {
                            // Get the transitions target state
                            S targetState = nfaGraph.getEdgeTarget(t);
                            Entry<S, V> target = new SimpleEntry<>(targetState, targetVertex);
                            Triplet<Entry<S, V>, Directed<E>> trip = new TripletImpl<>(source, triplet.getPredicate(), target);
                            MultiMaps.put(result, source, trip);
                            //result.put(source, trip);
                            //result.put(target, arg1);
                            //result.put(targetState, targetVertex);
                        }
                    });

                });
            });
        }

        return result;
        //Map<Entry<S, V>, <S, V>> vertextToPredecessor;



        //NfaExecutionUtils.advanceFrontier(frontier, nfaGraph, isEpsilon, getMatchingTriplets, pathGrouper, earlyPathReject)

        // PathExecutionUtils.execNfa(nfa, startStates, isEpsilon, startVertices, pathGrouper, getMatchingTriplets, pathCallback);


        // Map<V, Miap<S, Integer>> vertexToStateToCost
        // Map<V, Map<S, V>> vertexToStateToPredecessor

        /*
         * again, we need a successor function that fetche
         */
        //NfaExecution.nextFrontier();
    }

}
