package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.model.Triplet;
import org.aksw.jena_sparql_api.utils.model.TripletImpl;
import org.jgrapht.Graph;

public class JGraphTUtils {

//    public static <V, E> Graph<V, E> createReachabilityGraph(Graph<V, E> graph, Collection<V> start, Collection<V> end) {
//        List<NestedPath<V, E>> paths = getAllPaths(graph, start, end);
//
//
//
//
//    }

    public static <V, E> Triplet<V, E> toTriplet(Graph<V, E> graph, E edge) {
        V s = graph.getEdgeSource(edge);
        V t = graph.getEdgeTarget(edge);
        Triplet<V, E> result = new TripletImpl<>(s, edge, t);
        return result;
    }

    public static <V, E> List<NestedPath<V, E>> getAllPaths(Graph<V, E> graph, Collection<V> starts, Collection<V> ends) {
        SimplePathBfsIterator<V, E> it = new SimplePathBfsIterator<>(graph, starts,
                nestedPath -> ends.contains(nestedPath.getCurrent()));

        List<NestedPath<V, E>> result = new ArrayList<>();
        it.forEachRemaining(path -> result.addAll(path));
        return result;
    }

    public static <V, E> List<NestedPath<V, E>> getAllPaths(Graph<V, E> graph, V start, V end) {
        List<NestedPath<V, E>> result = getAllPaths(graph, Collections.singleton(start), Collections.singleton(end));
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


    public static <V, E> Set<V> transitiveGet(Graph<V, E> graph, Set<V> startVertices, int mode, Predicate<E> isEpsilon) {
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
    public static <V, E> Set<V> transitiveGet(Graph<V, E> graph, V startVertex, int mode, Predicate<E> edgeFilter) {
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

    public static <V, E> Set<E> resolveTransitions(Graph<V, E> graph, Predicate<E> isEpsilon, Collection<V> vertices, boolean reverse) {
        Set<E> result = vertices
                .stream()
                .flatMap(v -> resolveTransitions(graph, isEpsilon, v, reverse).stream())
                .collect(Collectors.toSet());

        return result;
    }

    /**
     * Returns the set of non-epsilon edges reachable via epsilon transitions from the given vertex
     *  // Check if a state is implicitly final if it has an epsilon transition to a final state
     */
    public static <V, E> Set<E> resolveTransitions(Graph<V, E> graph, Predicate<E> isEpsilon, V vertex, boolean reverse) {

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

    /**
     * Given a set of predicates and a direction,
     * return the set of predicates that can reach the target
     *
     */

    public static <V, E> void addSuperVertex(Graph<V, E> graph, V vertex, Set<V> fwdConns, Set<V> bwdConns) {
        addSuperVertex(graph, vertex, fwdConns, false);
        addSuperVertex(graph, vertex, fwdConns, true);
    }

    /**
     * Given a pair of predicates for fwd and backwards direction create
     * a new pair of those that can reach the target
     *
     */

    public static <V, E> void addSuperVertex(Graph<V, E> graph, V vertex, Set<V> conns, boolean reverse) {
        graph.addVertex(vertex);
        if(!reverse) {
            conns.forEach(conn -> {
                graph.addVertex(conn);
                graph.addEdge(vertex, conn);
            });
        } else {
            conns.forEach(conn -> {
                graph.addVertex(conn);
                graph.addEdge(conn, vertex);
            });
        }
    }

    public static <V, E> void addSuperVertex(Graph<V, E> graph, V vertex, V conn, boolean reverse) {
        addSuperVertex(graph, vertex, Collections.singleton(conn), reverse);
    }


}
