package org.aksw.jena_sparql_api.batch.cli.main;

import java.io.Closeable;
import java.util.Iterator;

import org.apache.jena.util.iterator.WrappedIterator;

public class ExtendedIteratorClosable<T>
    extends WrappedIterator<T>
{
    protected Closeable closable;

    public ExtendedIteratorClosable(Iterator<? extends T> base, Closeable closable) {
        super(base);
        this.closable = closable;
    }


    @Override
    public void close() {
        if(closable != null) {
            try {
                closable.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> ExtendedIteratorClosable<T> create(Iterator<? extends T> base, Closeable closable) {
        ExtendedIteratorClosable<T> result = new ExtendedIteratorClosable<>(base, closable);
        return result;
    }
}
