package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Multimap;

/**
 * TODO Refactor according to this:
 * The matching strategy combines a predicate for testing whether two lists of items have potential matches
 * together with function that can actually enumerate them
 * 
 * @author raven
 *
 * @param <A>
 * @param <B>
 */
@FunctionalInterface
public interface MatchingStrategyFactory<A, B>
    //extends BiFunction<List<A>, List<B>, Multimap<A, B>>
{
    boolean apply(List<A> as, List<B> bs, Multimap<A, B> mapping);
}




interface IterableUnknownSize<T>
    extends Iterable<T>
{
    /**
     * Function to indicate that there may be items in the stream.
     * Avoids potential needless expensive computations by .stream() 
     * @return
     */
    boolean mayHaveItems();
}

class IterableUnknowSimple<T>
    implements IterableUnknownSize<T>
{
    protected Boolean mayHaveItems = null;
    protected Iterable<T> delegate;
    
    public IterableUnknowSimple(Boolean mayHaveItems,
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
}
