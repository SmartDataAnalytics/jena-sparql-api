package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.Path;

public interface PropertySchema {

    Node getPredicate();
    boolean isForward();

    Path getPath();

    Set<? extends NodeSchema> getTargetSchemas();

    boolean canMatchTriples();

    boolean matchesTriple(Node source, Triple triple);

    long copyMatchingValues(Node source, Collection<Node> target, Graph sourceGraph);

    /**
     * Return a stream of the triples in sourceGraph that match this predicate
     * schema for the given starting node.
     *
     * @param source
     * @param sourceGraph
     * @return
     */
    public Stream<Triple> streamMatchingTriples(Node source, Graph sourceGraph);

    /**
     * Copy triples that match the predicate specification from the source graph
     * into the target graph.
     *
     * @param target
     * @param source
     */
    public long copyMatchingTriples(Node source, Graph targetGraph, Graph sourceGraph);
}
