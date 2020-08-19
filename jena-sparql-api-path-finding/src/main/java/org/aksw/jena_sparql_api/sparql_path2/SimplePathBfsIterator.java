package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.model.Directed;
import org.jgrapht.Graph;

import com.google.common.collect.AbstractIterator;

public class SimplePathBfsIterator<V, E>
    extends AbstractIterator<List<NestedPath<V, E>>>
{
    protected Graph<V, E> graph;
    protected List<NestedPath<V, E>> frontier;
    protected Predicate<NestedPath<V, E>> isAccepted;

    public SimplePathBfsIterator(Graph<V, E> graph, Collection<V> start, Predicate<NestedPath<V, E>> isAccepted) {
        super();
        this.graph = graph;
        this.frontier = start.stream()
                .map(item -> new NestedPath<V, E>(item))
                .collect(Collectors.toList());
        this.isAccepted = isAccepted;
    }

    public static <V, E> List<NestedPath<V, E>> collectPaths(List<NestedPath<V, E>> paths, Predicate<NestedPath<V, E>> predicate) {
        List<NestedPath<V, E>> result = paths.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        return result;
    }

    public static <V, E> List<NestedPath<V, E>> advanceFrontier(Graph<V, E> graph, Collection<NestedPath<V, E>> paths) {
        List<NestedPath<V, E>> result = new ArrayList<>();

        for(NestedPath<V, E> path : paths) {
            V current = path.getCurrent();
            Set<E> edges = graph.outgoingEdgesOf(current);
            for(E edge : edges) {
                boolean isCycle = path.containsEdge(edge, false);
                V v = graph.getEdgeTarget(edge);
                if(!isCycle) {
                    NestedPath<V, E> nextPath = new NestedPath<>(new ParentLink<>(path, new Directed<>(edge, false)), v);
                    result.add(nextPath);
                }
            }
        }

        return result;
    }

    @Override
    protected List<NestedPath<V, E>> computeNext() {
        List<NestedPath<V, E>> result;

        while(true) {
            // Collect paths
            result = collectPaths(frontier, isAccepted);

            // Advance the frontier
            frontier = advanceFrontier(graph, frontier);

            if(!result.isEmpty()) {
                break;
            } else if(frontier.isEmpty()) {
                result = endOfData();
                break;
            }
        }

        return result;
    }


}
