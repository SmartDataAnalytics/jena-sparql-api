package org.aksw.jena_sparql_api.dboe;

import java.util.LinkedHashSet;
import java.util.Set;
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
    protected Set<Quad> set;

    public QuadTableWithInsertOrderPreservation(QuadTableCore delegate) {
        this(delegate, new LinkedHashSet<>());
    }

    public QuadTableWithInsertOrderPreservation(QuadTableCore delegate, Set<Quad> collection) {
        super();
        this.delegate = delegate;
        this.set = collection;
    }

    @Override
    public void clear() {
        delegate.clear();
        set.clear();
    }

    @Override
    public void add(Quad quad) {
        delegate.add(quad);
        set.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        delegate.delete(quad);
        set.remove(quad);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        boolean matchesAny = Node.ANY.matches(g) && Node.ANY.matches(s) && Node.ANY.matches(p) && Node.ANY.matches(o);
        Stream<Quad> result = matchesAny
                ? set.stream()
                : delegate.find(g, s, p, o);
        return result;
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return delegate.listGraphNodes();
    }
}
