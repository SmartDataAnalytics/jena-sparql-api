package org.aksw.jena_sparql_api.util.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.aksw.commons.collections.cache.BlockingCacheIterator;
import org.aksw.commons.collections.cache.Cache;
import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache.CacheEntry;
import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;


class LazyLoadingCachingListIterator<T>
    extends AbstractIterator<T>
    implements ClosableIterator<T>
{
    protected Range<Long> canonicalRequestRange;
    //protected long upperBound;

    protected long offset;
    protected RangeMap<Long, RangedSupplierLazyLoadingListCache.CacheEntry<T>> rangeMap;
    protected Function<Range<Long>, ClosableIterator<T>> itemSupplier;

    public LazyLoadingCachingListIterator(
            Range<Long> canonicalRequestRange,
            RangeMap<Long, CacheEntry<T>> rangeMap,
            Function<Range<Long>, ClosableIterator<T>> itemSupplier) {
        super();
        this.canonicalRequestRange = canonicalRequestRange;
        this.rangeMap = rangeMap;
        this.itemSupplier = itemSupplier;

        this.offset = canonicalRequestRange.lowerEndpoint();
    }

    //protected Iterable</C>
    // Iterator for the fraction running from cache
    protected transient ClosableIterator<T> currentIterator;

    @Override
    public void close() {
        currentIterator.close();
    }

    @Override
    protected T computeNext() {
        T result;

        for(;;) {
        	boolean isOffsetInRequestRange = canonicalRequestRange.contains(offset);
            if(!isOffsetInRequestRange) {
                // TODO Use a cheaper primitive int / long comparison instead of the range
                // We hit the end of the requested iteration - exit
                currentIterator.close();

                result = endOfData();
                break;
            } else if(currentIterator == null) {

                // Make sure the map is not modified during lookup
                Entry<Range<Long>, CacheEntry<T>> e;
                synchronized(rangeMap) {
                    e = rangeMap.getEntry(offset);
                }

                // If there is no entry, consult the itemSupplier
                if(e == null) {
                    Range<Long> r = Range.atLeast(offset).intersection(canonicalRequestRange);
                    currentIterator = itemSupplier.apply(r);
                } else {
                    CacheEntry<T> ce = e.getValue();

                    // get the relative offset of
                    Range<Long> pageRange = ce.range;
                    long offsetWithinPage = offset - pageRange.lowerEndpoint();

                    Iterator<T> tmp = new BlockingCacheIterator<>(ce.cache, (int)offsetWithinPage);
                    currentIterator = new IteratorClosable<>(tmp);
                }
            } else if(currentIterator.hasNext()) {
                result = currentIterator.next();
                ++offset;
                break;
            } else { // if(!currentIterator.hasNext()) {
                // If the current iterator has no more items, we either
                // (a) have reached the end of a page and we need to advance to the next one
                // (b) there simple may not be any more data available

            	// In any case, close the current iterator
                currentIterator.close();

                if(isOffsetInRequestRange) {
                	// (b) is the case if the offset was within the requested range, but there were no items
                    result = endOfData();
                    break;
                }

                currentIterator = null;
            }
        }

        return result;
    }


}


/**
 * TODO Create an iterator that can trigger loading of further data once a certain amount has been consumed
 *
 * @author raven
 *
 * @param <T>
 */
public class RangedSupplierLazyLoadingListCache<T>
	implements RangedSupplier<Long, T>
{

    /**
     * RangeMap's .subRangeMap method may modify the first and last range due to
     * intersection with the requested range. Hence, we need to keep a copy
     * of the original range object
     * @author raven
     *
     * @param <T>
     */
    static class CacheEntry<T> {
        Range<Long> range;
        Cache<List<T>> cache;

        public CacheEntry(Range<Long> range, Cache<List<T>> cache) {
            super();
            this.range = range;
            this.cache = cache;
        }
    }

    static class RangeInfo<T> {
        Range<Long> range;
        boolean isGap;

        //CacheEntry<T> entry;
        Cache<List<T>> cache;

        public RangeInfo(Range<Long> range, boolean isGap,
                Cache<List<T>> cache) {
            super();
            this.range = range;
            this.isGap = isGap;
            this.cache = cache;
        }

        @Override
        public String toString() {
            return "[" + range + ", " + (isGap ? "gap" : "data") + "]";
        }
    }

    //protected ExecutorCompletionService<Object> executorService;
    protected ExecutorService executorService;
    //protected Function<Range<Long>, ClosableIterator<T>> itemSupplier;
    protected RangedSupplier<Long, T> itemSupplier;


    /**
     * Only items within this range will be cached in rangesToData
     *
     */
    protected Range<Long> cacheRange;


    //protected RangeSet<Long> cacheRanges;

    protected RangeCostModel costModel;

    protected RangeMap<Long, CacheEntry<T>> rangesToData;


    // We may dynamically discover that after certain offsets there is no more data
    protected Long dataThreshold = null;


    public static Range<Long> normalize(Range<Long> range) {
        range = RangeUtils.startFromZero(range);
        Range<Long> result = range.canonical(DiscreteDomain.longs());
    	return result;
    }

    /**
     * Expose whether a requested range can be answered only from the local storage - i.e. without a request to the underlying range supplier.
     *
     * @param range
     * @return
     */
    public boolean isCached(Range<Long> range) {
        range = normalize(range);

        RangeMap<Long, CacheEntry<T>> subRangeMap = rangesToData.subRangeMap(range);
    	Map<Range<Long>, CacheEntry<T>> x = subRangeMap.asMapOfRanges();

    	boolean result;
    	if(x.size() == 1) {
    		// Check if the range is covered AND the cache data is complete
    		Entry<Range<Long>, CacheEntry<T>> entry = x.entrySet().iterator().next();
    		CacheEntry<T> ce = entry.getValue();

    		boolean isEnclosing = ce.range.encloses(range);
    		// TODO Actually we do not need the whole range to be complete, but only the requested section has to be loaded yet
    		boolean isDataComplete = ce.cache.isComplete();

    		result = isEnclosing && isDataComplete;
    	} else {
    		result = false;
    	}

    	return result;
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> itemSupplier) {
    	this(executorService, itemSupplier, Range.atLeast(0l));
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> itemSupplier, Range<Long> range) {
    	this(executorService, itemSupplier, range, null);
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> itemSupplier, Range<Long> cacheRange, RangeCostModel costModel) {
        super();
        this.executorService = executorService;
        this.itemSupplier = itemSupplier;
        this.cacheRange = cacheRange;
        this.rangesToData = TreeRangeMap.create();
    }


    public ClosableIterator<T> apply(Range<Long> range) {
        range = normalize(range);

        ClosableIterator<T> result;
        if(range.isEmpty()) {
            result = new IteratorClosable<>(Collections.emptyIterator());
        } else {
            // Prevent changes to the map while we check its content
            synchronized(rangesToData) {
                Range<Long> lookupRange = range.intersection(cacheRange);
                RangeMap<Long, CacheEntry<T>> subMap = rangesToData.subRangeMap(lookupRange);

                List<RangeInfo<T>> rangeInfos = new ArrayList<>();
                // Determine the first offset of the query
                //Iterator<Entry<Range<Long>, CacheEntry<T>>> it = subMap.asMapOfRanges().entrySet().iterator();

                Long offset = range.lowerEndpoint();
                for(Entry<Range<Long>, CacheEntry<T>> e : subMap.asMapOfRanges().entrySet()) {
    //                Entry<Range<Long>, CacheEntry<T>> e = it.next();
                    Range<Long> eRange = e.getKey();
                    Cache<List<T>> cache = e.getValue().cache;
                    long ele = eRange.lowerEndpoint();


                    if(ele > offset) {
                        // We need to fetch a chunk at the beginning
                        Range<Long> gap = Range.closedOpen(offset, ele);
                        if(!gap.isEmpty()) {
                            rangeInfos.add(new RangeInfo<>(gap, true, null));
                        }
                        //Range<Long> r = Range.range(range.lowerEndpoint(), range.lowerBoundType(), eRange.lowerEndpoint(), )
                    } else {
                        rangeInfos.add(new RangeInfo<>(eRange, false, cache));
                    }

                    offset = eRange.hasUpperBound()
                            ? eRange.upperEndpoint()
                            : null;
                }

                if(offset != null && range.hasUpperBound()) {
                    Range<Long> lastGap = Range.closedOpen(offset, lookupRange.upperEndpoint());

                    if(!lastGap.isEmpty()) {
                        rangeInfos.add(new RangeInfo<>(lastGap, true, null));
                    }
                }

                // Prepare fetching of the gaps - this updates the map with additional entries
                fetchGaps(subMap, rangeInfos);
            }

            result = new LazyLoadingCachingListIterator<>(range, rangesToData, itemSupplier);
        }
        return result;
    }

    public void fetchGaps(RangeMap<Long, CacheEntry<T>> subMap, List<RangeInfo<T>> rangeInfos) {
        for(RangeInfo<T> rangeInfo : rangeInfos) {
            if(rangeInfo.isGap) {
                fetchGap(subMap, rangeInfo.range);
            }
        }
    }

    public void fetchGap(RangeMap<Long, CacheEntry<T>> subMap, Range<Long> range) {
        //System.out.println("GAP: " + range);
//
//
//
//        Range<Long> requestRange =
//                Iterables.getFirst(rangeInfos, null).range.span(Iterables.getLast(rangeInfos, null).range);

//        subMap.subRangeMap(range).clear();

        List<T> cacheData = new ArrayList<>();
        Cache<List<T>> cache = new Cache<>(cacheData);
        CacheEntry<T> cacheEntry = new CacheEntry<>(range, cache);
        subMap.put(range, cacheEntry);



        // Start a task to fill the cache
        ClosableIterator<T> ci = itemSupplier.apply(range);

        // TODO Return an iterator that triggers caching
        long maxCacheSize = cacheRange.hasUpperBound() ? cacheRange.upperEndpoint() : Long.MAX_VALUE;

        // Create an iterator for the given range
        Runnable task = () -> {

            try {
                long i = 0;
                int notificationInterval = 100;

                boolean hasMoreData;
                boolean isOk = true;
                boolean isTooBig = false;

                while((hasMoreData = ci.hasNext()) &&
                        !(isTooBig = i >= maxCacheSize) &&
                        (isOk = !(cache.isAbanoned() || Thread.interrupted()))) {
                    ++i;

                    T binding = ci.next();
                    cacheData.add(binding);

                    //System.out.println("Caching page " + range + " item " + i + ": " + binding);
                    if(i % notificationInterval == 0) {
	                    synchronized(cache) {
	                        cache.notifyAll();
	                    }
                    }
                }

                if(!hasMoreData) {
                    dataThreshold = dataThreshold == null || i < dataThreshold ? i : dataThreshold;
                }

                if(isOk) {
                    cache.setComplete(true);
                }

                if(isTooBig) {
                    // TODO Adjust the interval to the max cache size
                    // because for this interval the cache is complete

                    cache.setComplete(true);
                }

            } catch(Exception e) {
                cache.setAbanoned(true);
                throw new RuntimeException(e);
            } finally {
            	// Notify for a last time (if there is no data, this will actually also be the first notification for clients)
                synchronized(cache) {
                    cache.notifyAll();
                }

                ci.close();
            }
        };

        executorService.submit(task);

        //BlockingCacheIterator<T> cacheIt = new BlockingCacheIterator<>(cache);
    }

	@Override
    public <X> X unwrap(Class<X> clazz, boolean reflexive) {
    	@SuppressWarnings("unchecked")
		X result = reflexive && this.getClass().isAssignableFrom(clazz)
    		? (X)this
    		: itemSupplier.unwrap(clazz, true);

    	return result;
    }

	/**
	 * Static utility method to check whether a given range is cached by a ranged supplier by
	 * attempting to unwrap an instance of this class.
	 *
	 * TODO: Deal with range transformations
	 *
	 *
	 * @param rangedSupplier
	 * @param range
	 * @return
	 */
	public static <V> boolean isCached(RangedSupplier<Long, V> rangedSupplier, Range<Long> range) {
		@SuppressWarnings("unchecked")
		RangedSupplierLazyLoadingListCache<V> inst = rangedSupplier.unwrap(RangedSupplierLazyLoadingListCache.class, true);
		boolean result = inst.isCached(range);
		return result;
	}
}