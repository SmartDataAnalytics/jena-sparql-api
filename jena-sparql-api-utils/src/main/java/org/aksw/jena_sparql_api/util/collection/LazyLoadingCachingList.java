package org.aksw.jena_sparql_api.util.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.aksw.commons.collections.cache.Cache;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * TODO Create an iterator that can trigger loading of further data once a certain amount has been consumed 
 * 
 * @author raven
 *
 * @param <T>
 */
public class LazyLoadingCachingList<T> {
    
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
    protected Function<Range<Long>, ClosableIterator<T>> itemSupplier;


    /**
     * Only items within this range will be cached in rangesToData
     * 
     */
    protected Range<Long> cacheRange;
    protected RangeCostModel costModel;
    
    protected RangeMap<Long, CacheEntry<T>> rangesToData;

    
    // We may dynamically discover that after certain offsets there is no more data
    protected Long dataThreshold = null;
    
    public LazyLoadingCachingList(ExecutorService executorService, Function<Range<Long>, ClosableIterator<T>> itemSupplier, Range<Long> cacheRange, RangeCostModel costModel) {
        super();
        this.executorService = executorService;
        this.itemSupplier = itemSupplier;
        this.cacheRange = cacheRange;
        this.rangesToData = TreeRangeMap.create(); 
    }
    
    
    public ClosableIterator<T> retrieve(Range<Long> range) {
        range = RangeUtils.startFromZero(range);
        range = range.canonical(DiscreteDomain.longs());
        
        synchronized(this) {
            RangeMap<Long, CacheEntry<T>> subMap = rangesToData.subRangeMap(range);
                        
            
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
                    
                    rangeInfos.add(new RangeInfo<>(gap, true, null));
                    //Range<Long> r = Range.range(range.lowerEndpoint(), range.lowerBoundType(), eRange.lowerEndpoint(), )
                } else {
                    rangeInfos.add(new RangeInfo<>(eRange, false, cache));
                }
                
                offset = eRange.hasUpperBound()
                        ? eRange.upperEndpoint() + 1
                        : null;
            }

            if(offset != null && range.hasUpperBound()) {
                Range<Long> lastGap = Range.closedOpen(offset, range.upperEndpoint());
                
                rangeInfos.add(new RangeInfo<>(lastGap, true, null));
                
                //rangeInfos.add(new RangeInfo<>(Range.closedOpen(offset, range.upperEndpoint()))
            }

            Range<Long> requestRange =                    
                    Iterables.getFirst(rangeInfos, null).range.span(Iterables.getLast(rangeInfos, null).range);
            
            
            subMap.subRangeMap(requestRange).clear();

            List<T> cacheData = new ArrayList<>();
            Cache<List<T>> cache = new Cache<>(cacheData);
            CacheEntry<T> cacheEntry = new CacheEntry<>(range, cache); 
            subMap.put(requestRange, cacheEntry);
            
            
            
            // Start a task to fill the cache
            ClosableIterator<T> ci = itemSupplier.apply(requestRange);
            
            // TODO Return an iterator that triggers caching
            long maxCacheSize = cacheRange.hasUpperBound() ? cacheRange.upperEndpoint() : Long.MAX_VALUE;
            
            // Create an iterator for the given range
            Runnable task = () -> {
                
                try {
                    long i = 0;
                    
                    boolean hasMoreData;
                    boolean isOk = true;
                    boolean isTooBig = false;
                    
                    while((hasMoreData = ci.hasNext()) &&
                            !(isTooBig = i < maxCacheSize) &&
                            (isOk = !(cache.isAbanoned() || Thread.interrupted()))) {
                        ++i;
                        T binding = ci.next();
                        cacheData.add(binding);
                    }
        
                    if(!hasMoreData) {
                        dataThreshold = dataThreshold == null || i < dataThreshold ? i : dataThreshold;
                    }
                    
                    if(isOk) {
                        cache.setComplete(true);
                    }
                    
                    if(isTooBig) {
                        // TODO Pretend as if the
                        
                        cache.setComplete(true);
                    }
                    
                } catch(Exception e) {
                    cache.setAbanoned(true);
                    throw new RuntimeException(e);
                } finally {                    
                    ci.close();
                }
            };
                        
        }   
    
    
        return null;
    }
           
    
}