package org.aksw.jena_sparql_api_sparql_path2;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;

public class YensKShortestPaths {
//
//    public static List<TripletPath<Node, Node>> findPaths(
//            Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa,
//            Object isEpsilon,
//            Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass,
//            Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node, Node>>>> createTripletLookupService,
//            Node startNode, Node endNode, int maxK) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//

//    public static List<TripletPath<Node, Node>> findPaths(
//            Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa,
//            Predicate<T> isEpsilon,
//            Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass,
//            Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node, Node>>>> createTripletLookupService,
//            Node startNode, Node endNode, int maxK) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    public static <S, T, V, E> List<TripletPath<Entry<S, V>, Directed<E>>> findPaths(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Function<T, ? extends Pair<ValueSet<V>>> transToVertexClass,
            Function<Pair<ValueSet<V>>, ? extends Function<? super Iterable<V>, Map<V, Set<Triplet<V,E>>>>> createTripletLookupService,
            V source,
            V target,
            int maxK)
    {
        //BiFunction<Iterable<Entry<S, V>>, Integer, Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>>> successors =
        //NfaSuccessor<S, T, V, E> successors =
        Function<Iterable<Entry<S, V>>, Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>>> successors = new NfaSuccessor<S,T,V,E>(nfa, isEpsilon, transToVertexClass, createTripletLookupService);

        Set<Entry<S, V>> starts = new HashSet<>();
        nfa.getStartStates().forEach(s -> starts.add(new SimpleEntry<>(s, source)));

        List<TripletPath<Entry<S, V>, Directed<E>>> result = findPaths(
                successors,
                starts,
                e -> {
                    S state = e.getKey();
                    V vertex = e.getValue();

                    boolean isAcceptingState = NfaExecutionUtils.isFinalState(nfa, state, isEpsilon);//nfa.getEndStates().contains(state);
                    boolean isTargetVertex = target.equals(vertex);

                    boolean r = isAcceptingState && isTargetVertex;
                    return r;
                },
                maxK);

        return result;

    }

    // Adapted from https://en.wikipedia.org/wiki/Yen's_algorithm
    //Graph<V, E> graph,
    /**
     * The successor function maps each vertex to a set of triplets.
     * The triplets must have the vertex either as the subject or the object
     *
     *
     *
     * @param successors
     * @param source
     * @param target
     * @param maxK
     * @return
     */
    public static <V, E> List<TripletPath<V, Directed<E>>> findPaths(
            Function<? super Iterable<V>, Map<V, Set<Triplet<V, Directed<E>>>>> successors,
                    //Function<Iterable<Entry<S, V>>, Map<Entry<S, V>, Set<Triplet<Entry<S, V>, Directed<E>>>>>
            //BiFunction<Collection<Entry<S, V>>, Integer, Multimap<Entry<S, V>, Triplet<Entry<S, V>, Directed<E>>>>
            Collection<V> sources,
            Predicate<V> isTarget,
            int maxK)
    {
        List<TripletPath<V, Directed<E>>> A = new ArrayList<>();
        List<TripletPath<V, Directed<E>>> B = new ArrayList<>();

        Set<Triplet<V, Directed<E>>> removedTriplets = new HashSet<>();
        Set<V> removedNodes = new HashSet<V>();


        /**
         * Adapted successor function
         *
         */
        Function<Iterable<V>, Map<V, Set<Triplet<V, Directed<E>>>>> succ = rawNodes -> {
            Set<V> nodes = Lists.newArrayList(rawNodes).stream()
                    .filter(x -> !removedNodes.contains(x))
                    .collect(Collectors.toSet());


            Map<V, Set<Triplet<V, Directed<E>>>> tmp = successors.apply(nodes);
            tmp.entrySet().removeIf(removedNodes::contains);


            tmp.entrySet().forEach(e -> {
                Set<Triplet<V, Directed<E>>> ts = e.getValue();
                ts.removeIf(triplet -> {
                    boolean skip =
                            removedTriplets.contains(triplet) ||
                            removedNodes.contains(triplet.getSubject()) ||
                            removedNodes.contains(triplet.getObject());
                    return skip;
                });
            });


            return tmp;
            // TODO remove all removed edges from the successors
        };


        // Determine the shortest path from the source to the sink.
        TripletPath<V, Directed<E>> path = NfaDijkstra.dijkstra(successors, sources, isTarget);
        if(path != null) {
            A.add(path);


            for(int k = 1; k < maxK; ++k) {
                TripletPath<V, Directed<E>> ak_1 = A.get(k - 1);
                int akl = ak_1.getLength(); // TODO getLength returns only counts the triplets but not the vertices

                for(int i = 0; i < akl; ++i) {
                    V spurNode = ak_1.getNode(i);
                    TripletPath<V, Directed<E>> rootPath = ak_1.subPath(0, i);

                    for(TripletPath<V, Directed<E>> a : A) {
                        if(a.getLength() >= rootPath.getLength()) {
                            TripletPath<V, Directed<E>> subPath = a.subPath(0, i);
                            if(rootPath.equals(subPath))  { //p.nodes(0, i):
                            // Remove the links that are part of the previous shortest paths which share the same root path.
                            Triplet<V, Directed<E>> triplet = a.getTriplets().get(i); //get the triplet (i, i + 1)
                            //removedTriplets.add(Triplet.makeUndirected(triplet));
                                removedTriplets.add(triplet);
                            }
                        }
                    }

                    //for each node rootPathNode in rootPath except spurNode:
                    //    remove rootPathNode from Graph;
                    Set<V> rootPathNodes = rootPath.getNodeSet();
                    rootPathNodes.remove(spurNode);
                    removedNodes.addAll(rootPathNodes);

                    // Calculate the spur path from the spur node to the sink.
                    TripletPath<V, Directed<E>> spurPath = NfaDijkstra.dijkstra(succ, Collections.singleton(spurNode), isTarget);

                    if(spurPath != null) {
                        // Entire path is made up of the root path and spur path.
                        TripletPath<V, Directed<E>> totalPath = rootPath.concat(spurPath);
                        // Add the potential k-shortest path to the heap.
                        B.add(totalPath);
                    }

                    // Add back the edges and nodes that were removed from the graph.
                    removedNodes.clear();
                    removedTriplets.clear();
                }

                if(B.isEmpty()) {
                    // This handles the case of there being no spur paths, or no spur paths left.
                    // This could happen if the spur paths have already been exhausted (added to A),
                    // or there are no spur paths at all - such as when both the source and sink vertices
                    // lie along a "dead end".
                    break;
                }

                // Sort the potential k-shortest paths by cost.
                //B.sort();
                // (all costs are equal in our use case)

                // Add the lowest cost path becomes the k-shortest path.
                int l = B.size() - 1;
                TripletPath<V, Directed<E>> chosenPath = B.remove(l);
                        //B.get(l);
                A.add(chosenPath);
            }
        }

        return A;
    }


}

