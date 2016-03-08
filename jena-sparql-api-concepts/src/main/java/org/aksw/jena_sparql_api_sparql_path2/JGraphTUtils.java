package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

public class JGraphTUtils {

    public static <V, E> List<NestedPath<V, E>> getAllPaths(DirectedGraph<V, E> graph, V start, V end) {
        SimplePathBfsIterator<V, E> it = new SimplePathBfsIterator<>(graph, start, nestedPath -> nestedPath.getCurrent().equals(end));
        List<NestedPath<V, E>> result = new ArrayList<>();
        it.forEachRemaining(path -> result.addAll(path));
        return result;
    }

    public static <V, E> Set<V> targets(Graph<V, E> graph, Collection<E> edges) {
        Set<V> result = edges.stream()
                .map(e -> graph.getEdgeTarget(e))
                .collect(Collectors.toSet());
        return result;
    }

    public static <V, E> Set<V> sources(Graph<V, E> graph, Collection<E> edges) {
        Set<V> result = edges.stream()
                .map(e -> graph.getEdgeSource(e))
                .collect(Collectors.toSet());
        return result;
    }


    public static <V, E> Set<V> transitiveGet(DirectedGraph<V, E> graph, Set<V> startVertices, int mode, Predicate<E> isEpsilon) {
        Set<V> result = startVertices.stream()
                .flatMap(v -> transitiveGet(graph, v, mode, isEpsilon).stream())
                .collect(Collectors.toSet());
        return result;
    }

    /**
     * Simple transitive get function that retrieves all nodes reachable via
     * edges for which the predicate evaluates to true
     *
     * @param graph
     * @param startVertex
     * @param mode
     * @param edgeFilter
     * @return
     */
    public static <V, E> Set<V> transitiveGet(DirectedGraph<V, E> graph, V startVertex, int mode, Predicate<E> edgeFilter) {
        Set<V> result = new HashSet<V>();
        Set<V> open = new HashSet<V>(Collections.singleton(startVertex));

        while(!open.isEmpty()) {
            Iterator<V> it = open.iterator();
            V current = it.next();
            it.remove();

            boolean isVisited = result.contains(current);
            if(isVisited) {
                continue;
            }

            result.add(current);

            if(mode <= 0) {
                Collection<V> incoming =
                graph.incomingEdgesOf(current).stream()
                    .filter(edgeFilter)
                    .map(e -> graph.getEdgeSource(e))
                    .collect(Collectors.toList());

                open.addAll(incoming);
            }

            if(mode >= 0) {
                Collection<V> outgoing =
                graph.outgoingEdgesOf(current).stream()
                    .filter(edgeFilter)
                    .map(e -> graph.getEdgeTarget(e))
                    .collect(Collectors.toList());

                open.addAll(outgoing);
            }
        }

        return result;
    }

    public static <V, E> Set<E> resolveTransitions(DirectedGraph<V, E> graph, Set<V> vertices, Predicate<E> isEpsilon, boolean reverse) {
        Set<E> result = vertices
                .stream()
                .flatMap(v -> resolveTransitions(graph, v, isEpsilon, reverse).stream())
                .collect(Collectors.toSet());

        return result;
    }

    /**
     * Returns the set of non-epsilon edges reachable via epsilon transitions from the given vertex
     *  // Check if a state is implicitly final if it has an epsilon transition to a final state
     */
    public static <V, E> Set<E> resolveTransitions(DirectedGraph<V, E> graph, V vertex, Predicate<E> isEpsilon, boolean reverse) {

        Set<E> result = new HashSet<>();
        Set<E> visited = new HashSet<>();
        Set<E> open = new HashSet<>(reverse ? graph.incomingEdgesOf(vertex) : graph.outgoingEdgesOf(vertex));

        while(!open.isEmpty()) {
            Iterator<E> it = open.iterator();
            E edge = it.next();
            it.remove();

            boolean isVisited = visited.contains(edge);
            if(isVisited) {
                continue;
            }
            visited.add(edge);

            boolean isEps = isEpsilon.test(edge);
            V target = reverse ? graph.getEdgeSource(edge) : graph.getEdgeTarget(edge);

            if(isEps) {
                Set<E> nextEdges = reverse ? graph.incomingEdgesOf(vertex) : graph.outgoingEdgesOf(target);
                open.addAll(nextEdges);
            } else {
                result.add(edge);
            }
        }

        return result;
    }


}
