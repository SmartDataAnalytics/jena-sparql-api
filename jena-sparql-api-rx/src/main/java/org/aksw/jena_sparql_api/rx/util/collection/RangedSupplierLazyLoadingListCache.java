package org.aksw.jena_sparql_api.rx.util.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.aksw.commons.collections.cache.Cache;
import org.aksw.commons.collections.cache.CacheImpl;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.commons.rx.range.RangedSupplierDelegated;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.util.collection.CacheRangeInfo;
import org.aksw.jena_sparql_api.util.collection.RangeCostModel;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import io.reactivex.rxjava3.core.Flowable;


/**
 * TODO Create an iterator that can trigger loading of further data once a certain amount has been consumed
 *
 * @author raven
 *
 * @param <T>
 */
public class RangedSupplierLazyLoadingListCache<T>
    extends RangedSupplierDelegated<Long, T>
    implements CacheRangeInfo<Long> // TODO Turn into a list service - but then we have to change packages...
{

    static class RangeInfo<T> {
        Range<Long> range;
        boolean isGap;

        //CacheEntry<T> entry;
        Cache<T> cache;

        public RangeInfo(Range<Long> range, boolean isGap,
                Cache<T> cache) {
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
    //protected Function<Range<Long>, ClosableIterator<T>> delegate;


    /**
     * Only items within this range will be cached in rangesToData
     *
     */
    protected Range<Long> cacheRange;


    //protected RangeSet<Long> cacheRanges;

    protected RangeCostModel costModel;

    protected RangeMap<Long, CacheRangeEntry<T>> rangesToData;

    // We may dynamically discover that after certain offsets there is no more data
    protected Long dataThreshold = null;


    public static Range<Long> normalize(Range<Long> range) {
        range = RangeUtils.startFromZero(range);
        Range<Long> result = range.canonical(DiscreteDomain.longs());
        return result;
    }

    /**
     * Expose whether a requested range can be answered only from the local storage - i.e. without a request to the underlying range supplier.
     * The criteria are:
     * - dataThreshold is non null - so the end of the data has been seen
     * - all ranges up to the dataThreshold are consecutive and complete starting from 0
     *
     * @param range
     * @return
     */
    public boolean isCached(Range<Long> range) {
        range = normalize(range);

        // If we already know how much data there is, adjust the request to the available data
        if(dataThreshold != null) {
            Range<Long> dataRange = Range.closedOpen(0l, dataThreshold);
            range = range.intersection(dataRange);
        }
        //dataThreshold;


        RangeMap<Long, CacheRangeEntry<T>> subRangeMap = rangesToData.subRangeMap(range);
        Map<Range<Long>, CacheRangeEntry<T>> x = subRangeMap.asMapOfRanges();

        boolean result;
        if(x.size() == 1) {
            // Check if the range is covered AND the cache data is complete
            Entry<Range<Long>, CacheRangeEntry<T>> entry = x.entrySet().iterator().next();
            CacheRangeEntry<T> ce = entry.getValue();

            boolean isEnclosing = ce.range.encloses(range);
            // TODO Actually we do not need the whole range to be complete, but only the requested section has to be loaded yet
            boolean isDataComplete = ce.cache.isComplete();

            result = isEnclosing && isDataComplete;
        } else {
            result = false;
        }

        return result;
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> delegate) {
        this(executorService, delegate, Range.atLeast(0l));
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> delegate, Range<Long> range) {
        this(executorService, delegate, range, null);
    }

    public RangedSupplierLazyLoadingListCache(ExecutorService executorService, RangedSupplier<Long, T> delegate, Range<Long> cacheRange, RangeCostModel costModel) {
        super(delegate);
        this.executorService = executorService;
        //this.delegate = delegate;
        this.cacheRange = cacheRange;
        this.rangesToData = TreeRangeMap.create();
    }


    public Flowable<T> apply(Range<Long> range) {
        range = normalize(range);

        Flowable<T> result;
        if(range.isEmpty()) {
            result = Flowable.empty();//Stream.empty(); //new IteratorClosable<>(Collections.emptyIterator());
        } else {
            // Prevent changes to the map while we check its content
            synchronized(rangesToData) {
                Range<Long> lookupRange = range.intersection(cacheRange);
                RangeMap<Long, CacheRangeEntry<T>> subMap = rangesToData.subRangeMap(lookupRange);

                List<RangeInfo<T>> rangeInfos = new ArrayList<>();
                // Determine the first offset of the query
                //Iterator<Entry<Range<Long>, CacheEntry<T>>> it = subMap.asMapOfRanges().entrySet().iterator();

                Long offset = range.lowerEndpoint();
                for(Entry<Range<Long>, CacheRangeEntry<T>> e : subMap.asMapOfRanges().entrySet()) {
    //                Entry<Range<Long>, CacheEntry<T>> e = it.next();
                    Range<Long> eRange = e.getKey();
                    Cache<T> cache = e.getValue().cache;
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

                // The last gap is the intersection of the range starting at the current offset
                // with the lookupRange
                if(offset != null) {
                    Range<Long> part = Range.atLeast(offset);
                    Range<Long> lastGap = part.intersection(lookupRange);

                    if(!lastGap.isEmpty()) {
                        rangeInfos.add(new RangeInfo<>(lastGap, true, null));
                    }
                }

//                if(offset != null && range.hasUpperBound()) {
//                    Range<Long> lastGap = Range.closedOpen(offset, lookupRange.upperEndpoint());
//
//                    if(!lastGap.isEmpty()) {
//                        rangeInfos.add(new RangeInfo<>(lastGap, true, null));
//                    }
//                }

                // Prepare fetching of the gaps - this updates the map with additional entries
                fetchGaps(subMap, rangeInfos);
            }

            ClosableIterator<T> it = new LazyLoadingCachingListIterator<T>(range, rangesToData, delegate);
            result = Flowable.fromIterable(() -> it);
            result.doOnCancel(it::close);
            //result = org.aksw.jena_sparql_api.util.collection.StreamUtils.stream(it);
        }
        return result;
    }

    public void fetchGaps(RangeMap<Long, CacheRangeEntry<T>> subMap, List<RangeInfo<T>> rangeInfos) {
        for(RangeInfo<T> rangeInfo : rangeInfos) {
            if(rangeInfo.isGap) {
                fetchGap(subMap, rangeInfo.range);
            }
        }
    }

    // If we are requesting the last gap, we need to fetch one more item in order to decide whether
    // the cache is complete
    public void fetchGap(RangeMap<Long, CacheRangeEntry<T>> subMap, Range<Long> range) {
        //System.out.println("GAP: " + range);
//
//
//
//        Range<Long> requestRange =
//                Iterables.getFirst(rangeInfos, null).range.span(Iterables.getLast(rangeInfos, null).range);

//        subMap.subRangeMap(range).clear();

        List<T> cacheData = new ArrayList<>();
        Cache<T> cache = new CacheImpl<>(cacheData);
        CacheRangeEntry<T> cacheEntry = new CacheRangeEntry<>(range, cache);
        subMap.put(range, cacheEntry);



        // Start a task to fill the cache
        Flowable<T> stream = delegate.apply(range);
        Iterator<T> ci = stream.blockingIterable().iterator();

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
                        (isOk = !cache.isAbandoned() && !Thread.interrupted())) {
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

                    // TODO We can now adjust the reserved range
                    //subMap.remove(range, cacheEntry);
                }

                if(isOk) {
                    cache.setComplete();
                }

                if(isTooBig) {
                    // TODO Adjust the interval to the max cache size
                    // because for this interval the cache is complete

                    cache.setComplete();
                }

            } catch(Exception e) {
                cache.setAbandoned();
                throw new RuntimeException(e);
            } finally {
                // Notify for a last time (if there is no data, this will actually also be the first notification for clients)
                synchronized(cache) {
                    cache.notifyAll();
                }

                //ci.close();
                // Close underlying stream
                //stream.close();
            }
        };

        executorService.submit(task);

        //BlockingCacheIterator<T> cacheIt = new BlockingCacheIterator<>(cache);
    }

    @Override
    public String toString() {
        return "RangedSupplierLazyLoadingListCache [cacheRange=" + cacheRange
                + ", costModel=" + costModel + ", rangesToData=" + rangesToData + ", dataThreshold=" + dataThreshold + ", executorService=" + executorService
                + "]";
    }

//
//	@Override
//    public <X> Optional<X> unwrap(Class<X> clazz, boolean reflexive) {
//    	@SuppressWarnings("unchecked")
//		Optional<X> result = reflexive && clazz.isAssignableFrom(this.getClass())
//    		? Optional.of((X)this)
//    		: delegate.unwrap(clazz, true);
//
//    	return result;
//    }

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
//	public static <V> boolean isCached(RangedSupplier<Long, V> rangedSupplier, Range<Long> range) {
//		@SuppressWarnings("unchecked")
//		RangedSupplierLazyLoadingListCache<V> inst =
//		rangedSupplier.unwrap(RangedSupplierLazyLoadingListCache.class, true)
//			.ifPresent(inst -> inst.isCached(range))
//		boolean result = inst.isCached(range);
//		return result;
//	}
}