package org.aksw.state_space_search.core;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchUtils {
    public static <V, E, S> Stream<S> depthFirstSearch(
            V vertex,
            Predicate<V> isFinal,
            Function<V, S> vertexToResult,
            Function<V, Stream<E>> vertexToEdges,
            Comparator<E> edgeCostComparator,
            //Function<E, Comparable<?>> edgeToCost,
            Function<E, V> edgeToTargetVertex,
            int depth,
            int maxDepth) {

        Stream<S> result;
        boolean _isFinal = isFinal.test(vertex);
        if(_isFinal) {
            S value = vertexToResult.apply(vertex);
            result = Stream.of(value);
        }
        else if(depth > maxDepth) {
            result = Stream.empty();
        }
        else {
            Stream<E> edges = vertexToEdges.apply(vertex);

            List<E> x = edges.collect(Collectors.toList());
            System.out.println(x);
            edges = x.stream();

            int nextDepth = depth + 1;
            result = edges
                .sorted(edgeCostComparator)
                .flatMap(e -> {
                    V targetVertex = edgeToTargetVertex.apply(e);
                    Stream<S> r = depthFirstSearch(targetVertex,
                                isFinal,
                                vertexToResult,
                                vertexToEdges,
                                edgeCostComparator,
                                edgeToTargetVertex,
                                nextDepth,
                                maxDepth);
                    return r;
                })
                ;
        }

        return result;
    }



    public static <V, E, S> Stream<S> breadthFirstSearch(
            V vertex,
            Predicate<V> isFinal,
            Function<V, S> vertexToResult,
            Function<V, Stream<E>> vertexToEdges,
            Function<E, V> edgeToTargetVertex,
            int depth,
            int maxDepth) {
        Stream<S> result;
        boolean _isFinal = isFinal.test(vertex);
        if(_isFinal) {
            S value = vertexToResult.apply(vertex);
            result = Stream.of(value);
        }
        else if(depth > maxDepth) {
            result = Stream.empty();
        }
        else {
            Stream<E> edges = vertexToEdges.apply(vertex);

            int nextDepth = depth + 1;
            result = edges
                .map(edgeToTargetVertex::apply)
                .flatMap(s ->
                            breadthFirstSearch(s,
                                isFinal,
                                vertexToResult,
                                vertexToEdges,
                                edgeToTargetVertex,
                                nextDepth,
                                maxDepth))
                ;
        }

        return result;
    }
}
