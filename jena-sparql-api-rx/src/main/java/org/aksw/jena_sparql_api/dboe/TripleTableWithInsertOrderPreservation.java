package org.aksw.jena_sparql_api.dboe;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;


/**
 * Decorator for TripleTableCore whose add/delete methods also sync a set (typically a LinkedHashSet)
 * Invocation of .find() with only placeholders yields a stream from that set instead
 *
 * @author raven
 *
 */
public class TripleTableWithInsertOrderPreservation
    implements TripleTableCore
{
    protected TripleTableCore delegate;
    protected Set<Triple> triples;

    public TripleTableWithInsertOrderPreservation(TripleTableCore delegate) {
        this(delegate, new LinkedHashSet<>());
    }

    public TripleTableWithInsertOrderPreservation(TripleTableCore delegate, Set<Triple> collection) {
        super();
        this.delegate = delegate;
        this.triples = collection;
    }

    @Override
    public void clear() {
        delegate.clear();
        triples.clear();
    }

    @Override
    public void add(Triple quad) {
        delegate.add(quad);
        triples.add(quad);
    }

    @Override
    public void delete(Triple quad) {
        delegate.delete(quad);
        triples.remove(quad);
    }

    @Override
    public Stream<Triple> find(Node s, Node p, Node o) {
        boolean matchesAny = Node.ANY.matches(s) && Node.ANY.matches(p) && Node.ANY.matches(o);
        Stream<Triple> result = matchesAny
                ? triples.stream()
                : delegate.find(s, p, o);
        return result;
    }
}
