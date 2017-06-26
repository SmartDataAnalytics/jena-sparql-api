package org.aksw.combinatorics.algos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.combinatorics.collections.Combination;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.cache.CachingIterable;



/**
 * kPermutationsOfN implementation with support for solution computation and early bailout should a solution
 * turn out to be unsatisfiable.
 * At the core, this implementation notifies clients about results via a callback during a recursion.
 *
 * A static utility method is available which collects results into a list and returns a stream.
 * Hence, all valid permutations will be computed regardless of the number of items consumed from the stream.
 * However, as this approach is about 5-10 times faster than the recursive stream solution,
 * this approach is recommended.
 *
 *
 * @author raven
 *
 * @param <A>
 * @param <B>
 * @param <S>
 */
public class CartesianProductUtils<A, B, S> {

    public static <A, B, S> CartesianProduct<Combination<A, B, S>> createOnDemandCartesianProduct(
            Collection<A> as,
            Collection<B> bs,
            BiFunction<A, B, S> pairBasedSolver,
            Predicate<S> isUnsatisfiable
            ) {


        Function<A, Iterator<Combination<A, B, S>>> fn = (a) ->
            bs.stream()
                .map(b -> {
                    S s = pairBasedSolver.apply(a, b);
                    boolean unsatisfiable = isUnsatisfiable.test(s);
                    Combination<A, B, S> r = unsatisfiable
                            ? null
                            :  new Combination<A, B, S>(a, b, s);
                    return r;
                })
                .filter(x -> x != null)
                .iterator();

        List<Iterable<Combination<A, B, S>>> mappings = new ArrayList<>(as.size());
        for(A a : as) {
            Iterable<Combination<A, B, S>> foo = new CachingIterable<>(fn.apply(a));

            mappings.add(foo);
        }

        CartesianProduct<Combination<A, B, S>> result = CartesianProduct.create(mappings);
        return result;
    }
}
