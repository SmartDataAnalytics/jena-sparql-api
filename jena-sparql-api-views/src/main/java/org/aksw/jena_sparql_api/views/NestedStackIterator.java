package org.aksw.jena_sparql_api.views;

import java.util.Iterator;

public class NestedStackIterator<T>
    implements Iterator<T>
{
    protected NestedStack<T> current;

    public NestedStackIterator(NestedStack<T> current) {
        super();
        this.current = current;
    }

    @Override
    public boolean hasNext() {
        boolean result = current != null;
        return result;
    }

    @Override
    public T next() {
        T result = current.value;
        current = current.parent;
        return result;
    }
}
