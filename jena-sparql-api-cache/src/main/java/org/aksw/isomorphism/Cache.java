package org.aksw.isomorphism;

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

public class Cache<T>
//    implements Cache<T>
{
    //protected L data = new ArrayList<T>();
    protected T data;
    boolean isComplete = false;

    public Cache(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean status) {
        this.isComplete = status;
    }

//    public int size() {
//        int result = data.size();
//        return result;
//    }
}