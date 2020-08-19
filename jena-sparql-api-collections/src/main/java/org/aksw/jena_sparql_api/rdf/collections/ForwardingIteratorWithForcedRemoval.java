package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.NiceIterator;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;

/**
 * Iterator that forwards calls to another iterator. If it turns out that .remove() is not supported,
 * the remaining items in the iterator will be preloaded at once. Removal will then pass the current
 * item to a lambda that can remove the item from the underlying collection directly, such as by invoking
 * collection.remove(item).
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class ForwardingIteratorWithForcedRemoval<T>
    implements ClosableIterator<T>
{
    // Reference to the original delegate - delegate close
    protected Iterator<T> originalDelegate;

    protected Iterator<T> effectiveDelegate;
    protected Consumer<T> forceRemover;
    protected boolean delegateRemoval;

    protected boolean isNextCalled = false;
    protected T currentItem;

    public ForwardingIteratorWithForcedRemoval(Iterator<T> delegate, Consumer<T> forceRemover) {
        this(delegate, forceRemover, true);
    }

    public ForwardingIteratorWithForcedRemoval(Iterator<T> delegate, Consumer<T> forceRemover, boolean delegateRemoval) {
        super();
        this.originalDelegate = delegate;
        this.effectiveDelegate = delegate;
        this.forceRemover = forceRemover;
        this.delegateRemoval = delegateRemoval;
    }

    @Override
    public boolean hasNext() {
        boolean result = effectiveDelegate.hasNext();
        return result;
    }

    @Override
    public T next() {
        isNextCalled = true;
        currentItem = effectiveDelegate.next();

        return currentItem;
    }

    @Override
    public void remove() {
        if(delegateRemoval) {
            try {
                effectiveDelegate.remove();
            } catch(UnsupportedOperationException e) {
                List<T> prefetchedItems = Lists.newArrayList(effectiveDelegate);

                // Prefetch all remaining items of the iterator and use them to remove them from the underlying
                // collection
                // This avoids a concurrent modification exception
                effectiveDelegate = prefetchedItems.iterator();
                delegateRemoval = false;
            }
        }

        // Note that delegateRemoval may just have switched to false -
        // so this check is not redundant and thus cannot be converted to an else block
        if(!delegateRemoval) {
            if(!isNextCalled) {
                throw new IllegalStateException("Must call .next() in order to obtain a valid currentItem before calling .remove()");
            }

            isNextCalled = false;
            forceRemover.accept(currentItem);
        }
    }

    @Override
    public void close() {
        NiceIterator.close(originalDelegate);
    }
}
