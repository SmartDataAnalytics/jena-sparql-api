package org.aksw.jena_sparql_api.rx.util.collection;

import java.util.Map.Entry;

import org.aksw.commons.rx.range.RangedSupplier;

/**
 * Interface for obtaining a stream of entries for a given range.
 * Note totally sure whether this should extend from ranged supplier (as it is now)
 * or whether it should be separate from it.
 *
 * @author raven
 *
 * @param <I>
 * @param <K>
 * @param <V>
 */
public interface RangedEntrySupplier<I extends Comparable<I>, K, V>
    extends RangedSupplier<I, Entry<K, V>>
{
}
