package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
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
 * @author raven
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
    public static <T, V, E> boolean isOrigin(Pair<ValueSet<V>> vertexClass, V vertex, Triplet<V, E> triplet) { //T transition, Function<T, Pair<ValueSet<V>>> transToVertexClass,
        List<Boolean> dirs = Triplet.getDirections(triplet, vertex);
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
            Function<Pair<ValueSet<V>>, LookupService<V, Set<Triplet<V,E>>>> createTripletLookupService,
            V source,
            V target) {
        // We assign a cost to the vertex, but we not only need to track the preceeding vertex but also which edge was used
        Map<Entry<S, V>, Integer> vertexToCost = new HashMap<>();
        Map<Entry<S, V>, Triplet<Entry<S, V>, E>> vertexToMinCostPredecessor = new HashMap<>();


        Multimap<S, V> open = HashMultimap.create();


        //Set<Entry<S, V>> seen = new HashSet<>();


        //Multimap<Entry<S, V>, Triplet<Entry<S, V>, E>> result = HashMultimap.create();

        Frontier<Entry<S, V>> frontier = new FrontierImpl<>();
        nfa.getStartStates().forEach(s -> {
                Entry<S, V> e = new SimpleEntry<>(s, source);
                frontier.add(e);
                vertexToCost.put(e, 0);
        });

        //stateVertexPairs.forEach(e -> open.put(e.getKey(), e.getValue()));

        Entry<S, V> reachedTargetVertex = null;

        while(frontier.isEmpty()) { // TODO Pick vertex with lowest cost first
            Entry<S, V> currentVertex = frontier.next();
            int baseCost = vertexToCost.get(currentVertex);
            int thisCost = baseCost + 1;

            // Fetch the successors
            Multimap<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>> succs = getSuccessors(
                    nfa,
                    isEpsilon,
                    transToVertexClass,
                    createTripletLookupService,
                    open.entries());

            // For every successor check whether it can now be reached with a lower cost than before
            for(Entry<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>> entry : succs.entries()) {
                Entry<S, V> v = entry.getKey();
                Triplet<Entry<S, V>, Directed<E>> triplet = entry.getValue();

                Entry<S, V> succ = triplet.getObject();

                Number _targetMinCost = vertexToCost.getOrDefault(succ, null);

                int targetMinCost = _targetMinCost.intValue();

                if(thisCost < targetMinCost) {
                    vertexToCost.put(succ, thisCost);

                }

                // TODO Check if the desired target vertex was reached in an accepting state

            }



            //int alt

        }

        // Create the path by backtracking from the reached target vertex
        TripletPath<V, E> result;

        if(reachedTargetVertex != null) {
            List<Triplet<V, E>> path = new ArrayList<>();
            Entry<S, V> eo = reachedTargetVertex;
            while(eo != null) {
                Triplet<Entry<S, V>, E> predecessor = vertexToMinCostPredecessor.get(reachedTargetVertex);
                Entry<S, V> es = predecessor.getSubject();
                V s = es.getValue();
                E p = predecessor.getPredicate();
                V o = eo.getValue();

                path.add(new Triplet<V, E>(s, p, o));
                eo = es;
            }

            Collections.reverse(path);

            result = new TripletPath<>(source, target, path);
        } else {
            result = null;
        }

        return result;
    }


    public static <S, T, V, E>  Multimap<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>> getSuccessors(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, Pair<ValueSet<V>>> transToVertexClass,
            Function<Pair<ValueSet<V>>, LookupService<V, Set<Triplet<V,E>>>> createTripletLookupService,
            Collection<Entry<S, V>> stateVertexPairs
            )
    {
        // A graph for the data loaded so far
        //DirectedGraph<Entry<S, V>, Triplet<Entry<S, V>, E>> dynamicGraph = new DefaultDirectedGraph<>(Triplet.class);

        //Map<? super Entry<S, V>, Number> vertexToCost = new HashMap<>(); //Comparable<Number>

        //Set<Entry<S, V>> open = new HashSet<>();
        Multimap<S, V> open = HashMultimap.create();
        Multimap<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>> result = HashMultimap.create();

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

            // TODO Resolve transitions
            Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, isEpsilon, state, false);
            //Set<T> transitions = nfaGraph.outgoingEdgesOf(state);

            // TODO Factor out the part that fetches relations according to the nfa

            Pair<ValueSet<V>> vertexClass = transitions.stream().reduce(
                    (Pair<ValueSet<V>>)new VertexClass<V>(),
                    (a, b) -> transToVertexClass.apply(b),
                    (a, b) -> VertexClass.union(a, b));


            LookupService<V, Set<Triplet<V, E>>> tripletService = createTripletLookupService.apply(vertexClass);

            // For all nodes in the state
            Map<V, Set<Triplet<V, E>>> vToTriplets = tripletService.apply(vertices);

            // We now need to check which triplet matched which transition so that we associate the right successor state
            vToTriplets.entrySet().forEach(vToTriplet -> {
                V v = vToTriplet.getKey();
                Entry<S, V> source = new SimpleEntry<>(state, v);

                // for every individual transition
                transitions.stream().forEach(t -> {
                    Pair<ValueSet<V>> vc = transToVertexClass.apply(t);

                    // for every triplet check for its origin
                    vToTriplet.getValue().stream().forEach(rawTriplet -> {
                        Triplet<V, Directed<E>> triplet = Triplet.makeDirected(rawTriplet, v);
                        V targetVertex = triplet.getObject();
                        //V targetVertex = Triplet.getTarget(triplet, v);
                        //V targetVertex = Triplet.getTarget(triplet, reverse);
                        boolean isOrig = isOrigin(vc, v, triplet);
                        if(isOrig) {
                            // Get the transitions target state
                            S targetState = nfaGraph.getEdgeTarget(t);
                            Entry<S, V> target = new SimpleEntry<>(targetState, targetVertex);
                            Triplet<Entry<S, V>, Directed<E>> trip = new Triplet<>(source, triplet.getPredicate(), target);
                            result.put(source, trip);
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
