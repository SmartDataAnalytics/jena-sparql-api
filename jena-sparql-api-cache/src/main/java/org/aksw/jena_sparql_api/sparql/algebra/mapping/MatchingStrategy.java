package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.List;

import com.google.common.collect.Multimap;

@FunctionalInterface
public interface MatchingStrategy<A, B> {
    boolean apply(List<A> as, List<B> bs, Multimap<A, B> mapping);
}
