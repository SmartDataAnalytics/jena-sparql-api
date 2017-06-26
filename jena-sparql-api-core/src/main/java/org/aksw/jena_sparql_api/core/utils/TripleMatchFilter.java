package org.aksw.jena_sparql_api.core.utils;

import java.util.function.Predicate;

import org.apache.jena.graph.Triple;

/**
 * Backport of the triple match filter ; simply because it was referenced
 * in one place
 *
 * @author raven
 *
 */
public class TripleMatchFilter
    implements Predicate<Triple> {
    final protected Triple tMatch;

    /** Creates new TripleMatchFilter */
    public TripleMatchFilter(Triple tMatch) {
        this.tMatch = tMatch;
    }

    @Override
    public boolean test(Triple t) {
        return tMatch.matches(t);
    }

}
