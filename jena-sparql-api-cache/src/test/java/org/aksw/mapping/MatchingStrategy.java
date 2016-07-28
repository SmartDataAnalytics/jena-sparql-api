package org.aksw.mapping;

import java.util.List;

import com.google.common.collect.Multimap;

@FunctionalInterface
public interface MatchingStrategy<A, B> {
    boolean apply(List<A> as, List<B> bs, Multimap<A, B> mapping);
}
