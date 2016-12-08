package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.List;
import java.util.Map;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.Multimap;

/**
 * TODO Refactor according to this:
 * The matching strategy combines a predicate for testing whether two lists of items have potential matches
 * together with function that can actually enumerate them
 * 
 * @author raven
 *
 * @param <A>
 * @param <B>
 */
@FunctionalInterface
public interface MatchingStrategyFactory<A, B>
    extends TriFunction<List<A>, List<B>, Multimap<A, B>, Iterable<Map<A, B>>>
{
    //IterableUnknownSize<Map<A, B>> apply(List<A> as, List<B> bs, Multimap<A, B> mapping);
}


