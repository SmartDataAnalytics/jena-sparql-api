package org.aksw.isomorphism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.collections.CartesianProduct;
import org.apache.jena.ext.com.google.common.collect.Iterables;



/**
 * An iterable that caches (possibly computed) items returned from an underlying iterator, such that
 * subsequent iterations run from the cache.
 *
 * Useful for constructing cartesian products on-demand
 *
 * @author raven
 *
 * @param <T>
 */
class CachingIterable<T>
    implements Iterable<T>
{
    protected Iterator<T> delegate;
    protected Cache<T> cache = new Cache<>();

    public CachingIterable(Iterator<T> delegate) {
        super();
        this.delegate = delegate;
    }

    public static class Cache<T> {
        List<T> data = new ArrayList<T>();
        boolean isComplete = false;
    }

    public static class CachingIterator<T>
        implements Iterator<T>
    {
        protected Iterator<T> delegate;
        protected int i;

        protected Cache<T> cache;

        public CachingIterator(Cache<T> cache, Iterator<T> delegate) {
            super();
            this.cache = cache;
            this.delegate = delegate;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            boolean result;
            if(cache.isComplete) {
                result = i < cache.data.size();
            } else {
                result = delegate.hasNext();

                if(!result) {
                    cache.isComplete = true;
                }
            }

            return result;
        }

        @Override
        public T next() {
            T result;

            List<T> cacheData = cache.data;

            // Check if item at index i is already cached
            if(i < cacheData.size()) {
                result = cacheData.get(i);
            } else {
                result = delegate.next();
                cacheData.add(result);
            }

            ++i;
            return result;
        }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new CachingIterator<T>(cache, delegate);
        return result;
    }

    @Override
    public String toString() {
        String result = Iterables.toString(this);
        return result;
    }

}

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

            System.out.println(foo);

            mappings.add(foo);
        }

        CartesianProduct<Combination<A, B, S>> result = CartesianProduct.create(mappings);
        return result;
    }
}
