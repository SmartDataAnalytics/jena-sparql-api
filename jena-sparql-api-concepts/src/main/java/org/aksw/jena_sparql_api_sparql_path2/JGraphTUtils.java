package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.sparql.path.Path;
import org.jgrapht.DirectedGraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class JGraphTUtils {



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
    public static <V, T, E extends LabeledEdge<V, T>> Set<V> transitiveGet(DirectedGraph<V, E> graph, V startVertex, int mode, Predicate<E> edgeFilter) {
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
                    .map(e -> e.getSource())
                    .collect(Collectors.toList());

                open.addAll(incoming);
            }

            if(mode >= 0) {
                Collection<V> outgoing =
                graph.outgoingEdgesOf(current).stream()
                    .filter(edgeFilter)
                    .map(e -> e.getTarget())
                    .collect(Collectors.toList());

                open.addAll(outgoing);
            }
        }

        return result;
    }

    /**
     * Check if a state is implicitly final if it has an epsilon transition to a final state
     *
     */
    public static <V> Set<LabeledEdge<V, Path>> resolveTransitions(DirectedGraph<V, LabeledEdge<V, Path>> graph, V vertex) {

        Set<LabeledEdge<V, Path>> result = new HashSet<LabeledEdge<V, Path>>();

        Set<LabeledEdge<V, Path>> visited = new HashSet<LabeledEdge<V, Path>>();
        Set<LabeledEdge<V, Path>> open = new HashSet<LabeledEdge<V, Path>>(graph.outgoingEdgesOf(vertex));

        while(!open.isEmpty()) {
            Iterator<LabeledEdge<V, Path>> it = open.iterator();
            LabeledEdge<V, Path> edge = it.next();
            it.remove();

            boolean isVisited = visited.contains(edge);
            if(isVisited) {
                continue;
            }
            visited.add(edge);

            Path label = edge.getLabel();
            V target = edge.getTarget();

            if(label == null) {
                Set<LabeledEdge<V, Path>> nextEdges = graph.outgoingEdgesOf(target);
                open.addAll(nextEdges);
            } else {
                result.add(edge);
            }
        }

        return result;
    }


}
