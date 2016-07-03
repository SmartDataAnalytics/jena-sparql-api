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

    // Setting this flag is only valid if the cache is not completed yet
    // It indicates that no further items can be expected to be added to the cache
    // Hence, any blocking client should no longer wait for it but fail with an exception
    boolean isAbanoned = false;

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

    public boolean isAbanoned() {
        return isAbanoned;
    }

    public void setAbanoned(boolean isAbanoned) {
        this.isAbanoned = isAbanoned;
    }


//    public int size() {
//        int result = data.size();
//        return result;
//    }
}