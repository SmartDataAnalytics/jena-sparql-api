package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.Collections;
import java.util.Iterator;

public class IterableUnknownSizeSimple<T>
    implements IterableUnknownSize<T>
{
    protected Boolean mayHaveItems = null;
    protected Iterable<T> delegate;
    
    public IterableUnknownSizeSimple(Iterable<T> delegate) {
        this(null, delegate);
    }

    public IterableUnknownSizeSimple(Boolean mayHaveItems,
            Iterable<T> delegate) {
        super();
        this.mayHaveItems = mayHaveItems;
        this.delegate = delegate;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = delegate.iterator();
        if(mayHaveItems == null) {
            mayHaveItems = result.hasNext();
        }
        return result;
    }

    @Override
    public boolean mayHaveItems() {
        if(mayHaveItems == null) {
            // This initializes the mayHaveItems field
            iterator();
        }        
        return mayHaveItems;
    }   
    
    
    public static <T> IterableUnknownSizeSimple<T> createEmpty() {
        IterableUnknownSizeSimple<T> result = new IterableUnknownSizeSimple<>(false, Collections.emptySet());
        return result;
    }
}