package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;

public class YensKShortestPaths {

    // Adapted from https://en.wikipedia.org/wiki/Yen's_algorithm
//    public static <V, E> void findPaths(Graph<V, E> graph, V source, V target, int maxK) {
//        List<List<V>> A = new ArrayList<>();
//        List<List<V>> B = new ArrayList<>();
//
//        // Determine the shortest path from the source to the sink.
//        List<V> path = dijkstra(graph, source, target);
//
//        for(int k = 1; k < maxK; ++k) {
//            List<V> ak_1 = A.get(k - 1);
//            int akl = ak_1.size();
//
//            for(int i = 0; i < akl; ++i) {
//                V spurNode = ak_1.get(i);
//                List<V> rootPath = ak_1.subList(0, i);
//
//
//            }
//
//        }
//
//    }
//
//    function YenKSP(Graph, source, sink, K):
//        A[0] = Dijkstra(Graph, source, sink);
//        // Initialize the heap to store the potential kth shortest path.
//        B = [];
//
//        for k from 1 to K:
//            // The spur node ranges from the first node to the next to last node in the previous k-shortest path.
//            for i from 0 to size(A[k − 1]) − 1:
//
//                // Spur node is retrieved from the previous k-shortest path, k − 1.
//                spurNode = A[k-1].node(i);
//                // The sequence of nodes from the source to the spur node of the previous k-shortest path.
//                rootPath = A[k-1].nodes(0, i);
//
//                for each path p in A:
//                    if rootPath == p.nodes(0, i):
//                        // Remove the links that are part of the previous shortest paths which share the same root path.
//                        remove p.edge(i, i + 1) from Graph;
//
//                for each node rootPathNode in rootPath except spurNode:
//                    remove rootPathNode from Graph;
//
//                // Calculate the spur path from the spur node to the sink.
//                spurPath = Dijkstra(Graph, spurNode, sink);
//
//                // Entire path is made up of the root path and spur path.
//                totalPath = rootPath + spurPath;
//                // Add the potential k-shortest path to the heap.
//                B.append(totalPath);
//
//                // Add back the edges and nodes that were removed from the graph.
//                restore edges to Graph;
//                restore nodes in rootPath to Graph;
//
//            if B is empty:
//                // This handles the case of there being no spur paths, or no spur paths left.
//                // This could happen if the spur paths have already been exhausted (added to A),
//                // or there are no spur paths at all - such as when both the source and sink vertices
//                // lie along a "dead end".
//                break;
//            // Sort the potential k-shortest paths by cost.
//            B.sort();
//            // Add the lowest cost path becomes the k-shortest path.
//            A[k] = B[0];
//            B.pop();
//
//        return A;
}

