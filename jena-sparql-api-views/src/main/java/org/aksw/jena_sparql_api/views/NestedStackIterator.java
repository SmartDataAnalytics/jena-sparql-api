package org.aksw.jena_sparql_api.views;

import java.util.Iterator;

public class NestedStackIterator<T, P extends GenericNestedStack<T, P>>
    implements Iterator<T>
{
    protected GenericNestedStack<T, P> current;

    public NestedStackIterator(GenericNestedStack<T, P> current) {
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
