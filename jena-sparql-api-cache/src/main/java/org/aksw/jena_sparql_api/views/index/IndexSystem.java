package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;

/**
 * A datastructure which allows putting data of a type C into it,
 * and enables querying candidates with type Q.
 * 
 * Abstracts e.g. feature based indexing and retrieval of items
 *
 * @author raven
 *
 * @param <C> (Cache) Item object type
 * @param <Q> Query object type
 * @param <D> Type of the (D)ata associated with cache objects
 * @param <F> (F)eature type
 */
public interface IndexSystem<C, Q> {
    void add(C item);
    Collection<C> lookup(Q query);
}
