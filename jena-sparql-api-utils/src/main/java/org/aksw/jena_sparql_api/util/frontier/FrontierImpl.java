package org.aksw.jena_sparql_api.util.frontier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.google.common.collect.Sets;

public class FrontierImpl<T>
    implements Frontier<T>
{
    protected Collection<T> open;
    protected Collection<T> done;

    public FrontierImpl() {
        this(new HashSet<T>(), new HashSet<T>());
    }

    public FrontierImpl(Collection<T> open, Collection<T> done) {
        super();
        this.open = open;
        this.done = done;
    }

    @Override
    public void add(T item) {
        //Assert.notNull(item, "Cannot add a null item to frontier");

        boolean isAlreadyDone = done.contains(item);
        if(!isAlreadyDone) {
            open.add(item);
        }
    }

    @Override
    public T next() {
        T result;

        Iterator<T> it = open.iterator();
        if(it.hasNext()) {
            result = it.next();
            done.add(result);
            it.remove();

        } else {
            result = null;
        }

        return result;
    }

    @Override
    public boolean isEmpty() {
        boolean result = open.isEmpty();
        return result;
    }

    public static <T> FrontierImpl<T> createIdentityFrontier() {
        FrontierImpl<T> result = new FrontierImpl<T>(Sets.<T>newIdentityHashSet(), Sets.<T>newIdentityHashSet());
        return result;
    }

    @Override
    public FrontierStatus getStatus(Object item) {
        FrontierStatus result;
        boolean isDone = done.contains(item);
        boolean isOpen = open.contains(item);

        if(isDone) {
            if(isOpen) {
                throw new IllegalStateException();
            } else {
                result = FrontierStatus.DONE;
            }
        } else {
            result = isOpen ? FrontierStatus.OPEN : FrontierStatus.UNKNOWN;
        }

        return result;
    }

    @Override
    public void setStatus(T item, FrontierStatus status) {
        switch(status) {
            case DONE: {
                open.remove(item);
                done.add(item);
                break;
            }
            case OPEN: {
                done.remove(item);
                open.add(item);
                break;
            }
            case UNKNOWN: {
                done.remove(item);
                open.remove(item);
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public String toString() {
        return "FrontierImpl [open=" + open + ", done=" + done + "]";
    }
}
