package org.aksw.isomorphism;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Iterables;

/**
 * An iterable over an iterator that caches the iterator's results.
 *
 * @author raven
 *
 * @param <T>
 */
public class CachingIterable<T>
    implements Iterable<T>
{
    protected Iterator<T> delegate;
    protected Cache<? extends List<T>> cache; // = new Cache<T, C>();

    public CachingIterable(Iterator<T> delegate, Cache<? extends List<T>> cache) {
        super();
        this.delegate = delegate;
        this.cache = cache;
    }

    /**
     * In iterator that adds items to a cache as it proceeds.
     * It is valid for other iterators with the same delegate and cache to exist.
     * However, no iterator must be positioned beyond the cache.data's size.
     *
     *
     * @author raven
     *
     * @param <T>
     * @param <C>
     */
    public static class CachingIterator<T>
        implements Iterator<T>
    {
        protected Cache<? extends List<T>> cache;
        protected Iterator<T> delegate;
        protected int offset;

        public CachingIterator(Cache<? extends List<T>> cache, Iterator<T> delegate, int offset) {
            super();
            this.cache = cache;
            this.delegate = delegate;
            this.offset = offset;
        }

        /**
         * The cache's isComplete flag is only set if a call to hasNext returns false.
         */
        @Override
        public boolean hasNext() {
            boolean result;
            int cacheSize = cache.getData().size();
            if(offset < cacheSize) { // logical or: assuming offset == cache.size()
                result = true;
            } else if(cache.isComplete()) {
                result = false;
            } else {
                result = delegate.hasNext();

                if(!result) {
                    cache.setComplete(true);
                }
            }

            return result;
        }

        @Override
        public T next() {
            T result;

            List<T> cacheData = cache.getData();

            // Check if item at index i is already cached
            if(offset < cacheData.size()) {
                result = cacheData.get(offset);
            } else {
                result = delegate.next();
                cacheData.add(result);

                // Inform all possibly waiting client on the cache
                // that data has been added so that they can commence
                cache.notifyAll();
            }

            ++offset;
            return result;
        }
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new CachingIterator<T>(cache, delegate, 0);
        return result;
    }

    @Override
    public String toString() {
        String result = Iterables.toString(this);
        return result;
    }

//    public static <T> Iterable<T> newArrayListCachingIterable(Iterator<T> delegate) {
//        Cache<T> cache = new Cache<T, List<T>>(new ArrayList<T>());
//        Iterable<T> result = new CachingIterable<T, List<T>>(delegate, cache, 0);
//        return result;
//    }

}