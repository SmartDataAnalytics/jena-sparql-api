package org.aksw.jena_sparql_api.rx.util.collection;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.commons.collections.cache.IndexBasedIterator;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class LazyLoadingCachingListIterator<T>
    extends AbstractIterator<T>
    implements ClosableIterator<T>
{
    protected Range<Long> canonicalRequestRange;
    //protected long upperBound;

    protected long offset;
    protected RangeMap<Long, CacheRangeEntry<T>> rangeMap;
    protected Function<Range<Long>, Flowable<T>> delegate;

    protected boolean usedDelegate;

    public LazyLoadingCachingListIterator(
            Range<Long> canonicalRequestRange,
            RangeMap<Long, CacheRangeEntry<T>> rangeMap,
            Function<Range<Long>, Flowable<T>> delegate) {
        super();
        this.canonicalRequestRange = canonicalRequestRange;
        this.rangeMap = rangeMap;
        this.delegate = delegate;

        this.offset = canonicalRequestRange.lowerEndpoint();

        this.usedDelegate = false;
    }

    //protected Iterable</C>
    // Iterator for the fraction running from cache
    protected transient ClosableIterator<T> currentIterator;
    //protected transient Stream<T> currentIterator;

    @Override
    public void close() {
        currentIterator.close();
    }

    @Override
    protected T computeNext() {
        T result;

        for(;;) {
            boolean isOffsetInRequestRange = canonicalRequestRange.contains(offset);
            if (!isOffsetInRequestRange) {
                // TODO Use a cheaper primitive int / long comparison instead of the range
                // We hit the end of the requested iteration - exit
                currentIterator.close();

                result = endOfData();
                break;
            } else if(currentIterator == null) {

                // Make sure the map is not modified during lookup
                Entry<Range<Long>, CacheRangeEntry<T>> e;
                synchronized(rangeMap) {
                    e = rangeMap.getEntry(offset);
                }

                // If there is no entry, consult the delegate - if it is present and was not used yet
                // Otherwise, we are out of data
                if(e == null) {
                    if(delegate != null && !usedDelegate) {
                        Range<Long> r = Range.atLeast(offset).intersection(canonicalRequestRange);
                        Flowable<T> stream = delegate.apply(r);
                                // .takeWhile(x -> !cancelled[0]);
                        
                        Iterator<T> it = stream.blockingIterable().iterator();
                        Disposable disposable = (Disposable)it;

                        currentIterator = new IteratorClosable<>(it, () -> disposable.dispose()); //stream::close);
                        usedDelegate = true;
                    } else {
                        result = endOfData();
                        break;
                    }
                } else {
                    CacheRangeEntry<T> ce = e.getValue();

                    // get the relative offset of
                    Range<Long> pageRange = ce.range;
                    long offsetWithinPage = offset - pageRange.lowerEndpoint();

                    Iterator<T> tmp = new IndexBasedIterator<>(ce.cache, (int)offsetWithinPage);//new BlockingCacheIterator<>(ce.cache, (int)offsetWithinPage);
                    currentIterator = new IteratorClosable<>(tmp);

                    // The range may be bigger than the data contained in it.
                    if(!currentIterator.hasNext()) {
                        result = endOfData();
                        currentIterator.close();
                        break;
                    }

                }
            } else if (currentIterator.hasNext()) {
                result = currentIterator.next();
                ++offset;
                break;
            } else { // if(!currentIterator.hasNext()) {
                // If the current iterator has no more items, we either
                // (a) have reached the end of a page and we need to advance to the next one
                // (b) there simple may not be any more data available

                // In any case, close the current iterator
                currentIterator.close();

//                if(isOffsetInRequestRange) {
//                	// (b) is the case if the offset was within the requested range, but there were no items
//                    result = endOfData();
//                    break;
//                }

                currentIterator = null;
            }
        }

        return result;
    }
}