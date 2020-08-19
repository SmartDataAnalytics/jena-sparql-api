package org.aksw.jena_sparql_api.pagination.core;

import org.aksw.commons.collections.PrefetchIterator;
import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.util.iterator.ClosableIterator;

/**
 * Implements jena interfaces to work with Iter.close(...) and
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class PrefetchIteratorForJena<T>
    extends PrefetchIterator<T>
    implements ClosableIterator<T>, Closeable
{

}
