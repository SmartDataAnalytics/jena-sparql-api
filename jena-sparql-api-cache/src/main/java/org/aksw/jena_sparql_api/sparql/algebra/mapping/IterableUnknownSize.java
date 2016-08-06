package org.aksw.jena_sparql_api.sparql.algebra.mapping;

public interface IterableUnknownSize<T>
    extends Iterable<T>
{
    /**
     * Function to indicate that there may be items in the stream.
     * Avoids potential needless expensive computations by .stream() 
     * @return
     */
    boolean mayHaveItems();
}