package org.aksw.jena_sparql_api.dboe;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public interface TripleTableCore {
    void clear();
    void add(Triple triple);
    void delete(Triple triple);
    Stream<Triple> find(Node s, Node p, Node o);

    default Stream<Triple> find() {
        return find(Node.ANY, Node.ANY, Node.ANY);
    }


    default boolean isEmpty() {
        return !find(Node.ANY, Node.ANY, Node.ANY)
                .findAny().isPresent();
    }

    default boolean contains(Triple triple) {
        return find(triple.getSubject(), triple.getPredicate(), triple.getObject())
                .findAny().isPresent();

//  Add try-with-resources in order to close streams?
//        boolean result;
//        try (Stream<Triple> stream = find(triple.getSubject(), triple.getPredicate(), triple.getObject())) {
//            result = stream.findAny().isPresent();
//        }
//        return result;
    }
}