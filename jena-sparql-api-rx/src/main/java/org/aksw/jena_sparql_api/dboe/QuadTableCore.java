package org.aksw.jena_sparql_api.dboe;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Core interface for a collection of quads
 * Be aware that it can serve on multiple API contracts by either allowing/exposing or disallowing/hiding a default graph
 *
 * @author raven
 *
 */
public interface QuadTableCore {
    void clear();
    void add(Quad quad);
    void delete(Quad quad);
    Stream<Quad> find(Node g, Node s, Node p, Node o);

    Stream<Node> listGraphNodes();

    default boolean isEmpty() {
        return !find(Node.ANY, Node.ANY, Node.ANY, Node.ANY)
                .findAny().isPresent();
    }

    default boolean contains(Quad quad) {
        boolean result = find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject())
            .findAny().isPresent();
        return result;
    }

    default void deleteGraph(Node g) {
        List<Quad> list = find(g, Node.ANY, Node.ANY, Node.ANY).collect(Collectors.toList());
        for (Quad quad : list) {
            delete(quad);
        }
    }
}
