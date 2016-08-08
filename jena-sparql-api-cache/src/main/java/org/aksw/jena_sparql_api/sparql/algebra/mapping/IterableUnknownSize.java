package org.aksw.jena_sparql_api.sparql.algebra.mapping;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IterableUnknownSize<T>
    extends Iterable<T>
{
    /**
     * Function to indicate that there may be items in the stream.
     * Avoids potential needless expensive computations by .stream() 
     * @return
     */
    boolean mayHaveItems();
    
    default Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}