package org.aksw.jena_sparql_api.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.util.iterator.ClosableIterator;


public class IteratorClosable<T>
    implements ClosableIterator<T>
{
    private Iterator<T> delegate;
    private Closeable closable;

    public IteratorClosable(Iterator<T> delegate) {
        this(delegate, null);
    }
    
    public IteratorClosable(Iterator<T> delegate, Closeable closable) {
        super();
        this.delegate = delegate;
        this.closable = closable;
    }

    @Override
    public boolean hasNext() {
        boolean result = delegate.hasNext();
        return result;
    }

    @Override
    public T next() {
        T result = delegate.next();
        return result;
    }

    @Override
    public void remove() {
        delegate.remove();
    }

    @Override
    public void close() {
        if(closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

