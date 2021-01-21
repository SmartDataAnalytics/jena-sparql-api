package org.aksw.jena_sparql_api.mapper.parallel;

/**
 * Interface for Alt stores. Note that Alt2 and Alt3 are essentially tuples and triples
 * with arbitrary types for each component.
 *
 * @author raven
 *
 */
public interface Alt
    // extends List<Object> -- if desired then extend from List
{
    /**
     * @return The item with the given index in this Alt instance
     */
    Object get(int index);

    /**
     * @return The number of items in this Alt instance
     */
    int size();
}
