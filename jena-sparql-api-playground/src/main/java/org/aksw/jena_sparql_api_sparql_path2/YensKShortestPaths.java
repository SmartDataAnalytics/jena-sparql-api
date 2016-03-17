package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;

public class YensKShortestPaths {

    // Adapted from https://en.wikipedia.org/wiki/Yen's_algorithm
    //Graph<V, E> graph,
    public static <V, E> List<TripletPath<V, Directed<E>>> findPaths(Function<Iterable<V>, Map<V, Set<Triplet<V, E>>>> successors, V source, V target, int maxK) {
        List<TripletPath<V, Directed<E>>> A = new ArrayList<>();
        List<TripletPath<V, Directed<E>>> B = new ArrayList<>();

        Set<Triplet<V, E>> removedTriplets = new HashSet<>();
        Set<V> removedNodes = new HashSet<V>();


        /**
         * Adapted successor function
         *
         */
        Function<Iterable<V>, Map<V, Set<Triplet<V, E>>>> succ = rawNodes -> {
            Set<V> nodes = Lists.newArrayList(rawNodes).stream()
                    .filter(x -> !removedNodes.contains(x))
                    .collect(Collectors.toSet());

            Map<V, Set<Triplet<V, E>>> tmp = successors.apply(nodes);
            return tmp;
            // TODO remove all removed edges from the successors
        };


        // Determine the shortest path from the source to the sink.
        TripletPath<V, Directed<E>> path = NfaDijkstra.dijkstra(successors, source, target);


        for(int k = 1; k < maxK; ++k) {
            TripletPath<V, Directed<E>> ak_1 = A.get(k - 1);
            int akl = ak_1.getLength(); // TODO getLength returns only counts the triplets but not the vertices

            for(int i = 0; i < akl; ++i) {
                V spurNode = ak_1.getNode(i);
                TripletPath<V, Directed<E>> rootPath = ak_1.subPath(0, i);

                for(TripletPath<V, Directed<E>> a : A) {
                    TripletPath<V, Directed<E>> subPath = a.subPath(0, i);
                    if(rootPath.equals(subPath))  { //p.nodes(0, i):
                        // Remove the links that are part of the previous shortest paths which share the same root path.
                        Triplet<V, Directed<E>> triplet = a.getTriplets().get(i); //get the triplet (i, i + 1)
                        removedTriplets.add(Triplet.makeUndirected(triplet));
                    }
                }

                //for each node rootPathNode in rootPath except spurNode:
                //    remove rootPathNode from Graph;
                Set<V> rootPathNodes = rootPath.getNodeSet();
                rootPathNodes.remove(spurNode);
                removedNodes.addAll(rootPathNodes);

                // Calculate the spur path from the spur node to the sink.
                TripletPath<V, Directed<E>> spurPath = NfaDijkstra.dijkstra(succ, spurNode, target);

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
            A.add(B.get(l));
            B.remove(l);

        }

        return A;
    }




}

