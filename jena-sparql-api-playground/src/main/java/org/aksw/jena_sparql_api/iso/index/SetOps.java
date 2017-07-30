package org.aksw.jena_sparql_api.iso.index;

import java.util.function.Function;

import com.google.common.collect.BiMap;

public interface SetOps<S, I> {
    S createNew();
    S intersect(S a, S b);
    S difference(S a, S b);
    //S transformItems(S a, Function<I, I> itemTransform);

    S transformItems(S a, Function<I, I> nodeTransform);

    /**
     * Apply a (partial) isomorphic mapping
     * Any non-mapped item is treated as if mapped to itself.
     *
     * Note: Given a set {a, b) and the mapping {a -> b}
     * the reverse mapping of b would be ambiguous.
     *
     * The advantage of this method over the generic transformation is,
     * that the resulting set can be merely a view
     * rather than a copy
     *
     * @param a
     * @param itemTransform
     * @return
     */
    default S applyIso(S a, BiMap<I, I> itemTransform) {
        S result = transformItems(a, itemTransform::get);
        return result;
    }

    int size(S g);
}
