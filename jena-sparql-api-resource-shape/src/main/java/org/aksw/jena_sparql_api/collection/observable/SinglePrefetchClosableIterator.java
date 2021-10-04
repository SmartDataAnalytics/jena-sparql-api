package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.WrappedIterator;


/**
 * A SinglePrefetchIterator that implements ClosableIterator such that
 * when wapping using {@link WrappedIterator#wrap} the close method
 * gets called through the wrapped.
 * 
 * @author raven
 *
 * @param <T>
 */
public abstract class SinglePrefetchClosableIterator<T>
	extends SinglePrefetchIterator<T>
	implements ClosableIterator<T>
{
}
