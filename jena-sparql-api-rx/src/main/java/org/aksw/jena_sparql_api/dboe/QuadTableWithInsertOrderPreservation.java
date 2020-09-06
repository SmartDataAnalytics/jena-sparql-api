package org.aksw.jena_sparql_api.dboe;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Forwarding QuadTableCore that additionally tracks quads in a collection (typically a LinkedHashSet)
 * Calling .find() with Node.ANY in all places yields a stream from the collection instead
 *
 * @author raven
 *
 */
public class QuadTableWithInsertOrderPreservation
    implements QuadTableCore
{
    protected QuadTableCore delegate;
    protected Collection<Quad> collection;

    public QuadTableWithInsertOrderPreservation(QuadTableCore delegate) {
        this(delegate, new LinkedHashSet<>());
    }

    public QuadTableWithInsertOrderPreservation(QuadTableCore delegate, Collection<Quad> collection) {
        super();
        this.delegate = delegate;
        this.collection = collection;
    }

    @Override
    public void clear() {
        delegate.clear();
        collection.clear();
    }

    @Override
    public void add(Quad quad) {
        delegate.add(quad);
        collection.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        delegate.delete(quad);
        collection.remove(quad);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        boolean matchesAny = Node.ANY.matches(g) && Node.ANY.matches(s) && Node.ANY.matches(p) && Node.ANY.matches(o);
        Stream<Quad> result = matchesAny
                ? collection.stream()
                : delegate.find(g, s, p, o);
        return result;
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return delegate.listGraphNodes();
    }
}
