package org;

import java.util.Collection;
import java.util.Map.Entry;

/**
 *
 * @author raven
 *
 * @param <C> (Cache) Item object type
 * @param <Q> Query object type
 * @param <D> Type of the (D)ata associated with cache objects
 * @param <F> (F)eature type
 */
public interface IndexSystem<C, Q, D> {
    void put(C item, D data);
    Collection<Entry<C, D>> lookup(Q query);
}
