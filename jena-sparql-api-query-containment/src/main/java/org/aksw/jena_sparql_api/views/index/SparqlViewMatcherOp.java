package org.aksw.jena_sparql_api.views.index;

import java.util.Map;

import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.apache.jena.sparql.algebra.Op;

/**
 * Interface for algebra based view matchers.
 * Methods and behavior are similar to that of a map which takes
 * custom keys and Op objects as value.
 * However, lookup methods are provided for retrieving matches (i.e. entries + metadata about the match)
 * by an algebra expression.
 * Implementations of these methods are expected to perform a best-effort in yielding entries, whose
 * associated algebra expression is a sub-tree isomorphism of the given expression.
 *
 *
 *
 * @author raven
 *
 * @param <K>
 */
public interface SparqlViewMatcherOp<K>
    extends PatternIndex<K, Op, OpVarMap>
{
}
