package org;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorCompletionService;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.cache.Cache;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;



class QueryExecutionSliceSupplier
    implements Function<Range<Long>, QueryExecution>
{
    protected QueryExecutionFactory qef;
    protected Query queryTemplate;
    
    @Override
    public QueryExecution apply(Range<Long> range) {
        Query query = queryTemplate.cloneQuery();
        QueryUtils.applyRange(query, range);

        QueryExecution result = qef.createQueryExecution(query);
        
        return result;
    }
}


/**
 * Iterator that changes its delegate in case there is a problem with the current one 
 * 
 * @author raven
 *
 * @param <T>
 */
//class FallbackIterator<T, I extends Iterator<T>>
//    extends AbstractIterator<T>
//{
//    protected Iterator<T> currentDelegate;
//    protected Function<I, I> fallback; 
//    
//
//    @Override
//    protected T computeNext() {
//
//        if(currentDelegate.hasNext()) {
//            T result;
//            try {
//                result = currentDelegate.next();
//            } catch(Exception e) {
//                currentDelegate = fallback.apply(currentDelegate);            
//            }
//        } else {
//            result = endOfData();
//        }
//        
//        return result;
//    }
//    
//    
//    
//}

//class RangeItem<R extends Comparable<R>, T> {
//    protected Range<R> range;
//    protected T data;
//    
//    public CacheEntry(Range<R> range, T data) {
//        super();
//        this.range = range;
//        this.data = data;
//    }
//
//    public Range<R> getRange() {
//        return range;
//    }
//
//    public T getData() {
//        return data;
//    }
//    
//     
//    
//}
//
//interface RangeIndex<K extends Comparable<K>, V> {
//    //void put(Range<K> range, V value);
////    getAllOverlappingRanges(Range<K> range);
//    RangeIndex<K, V> intersectingEntries(Range<K> range);
//}
//
//class RangeMapSimple<K extends Comparable<K>, V>
//    extends AbstractMap<Range<K>, V>
//    implements RangeIndex<K, V>
//{
//    protected Set<Entry<Range<K>, V>> entries;
//
//    
//    public RangeMapSimple() {
//        this(new HashMap<>());
//    }
//
//    public RangeMapSimple(Set<Entry<Range<K>, V>> entries) {
//        super();
//        this.entries = entries;
//    }
//
//    @Override
//    public V put(Range<K> key, V value) {
//        Entry<Range<K>, V> e = new SimpleEntry<>(key, value);
//        entries.add(e);
//        return value;
//    }
//    
//    public RangeMap<K, V> intersectingEntries(Range<K> range) {
//        Set<Entry<Range<K>, V>> matches = RangeUtils.getIntersectingRanges(range, entries);
//        
//    }
//    
//    @Override
//    public Set<Entry<Range<K>, V>> entrySet() {
//        return entries;
//    }    
//}
//


class RangeCostModel {

    /**
     * The maximum cost for waiting for an intersecting interval before the requested range
     * to become available
     */
    protected double waitCostThreshold;
    
    
    /**
     * The cost of iterating items [TODO this is bull]
     */
    protected Function<Range<Long>, Double> rangeToIterCost; 
    
    /**
     * The cost of starting a request a given offset (independent of the range of items being fetched)
     * 
     */
    protected Function<Long, Double> offsetToQueryCost;
}




/**
 * TODO Create an iterator that can trigger loading of further data once a certain amount has been consumed 
 * 
 * @author raven
 *
 * @param <T>
 */
class SegmentedList<T> {
    
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
        
        
    }
    
    protected ExecutorCompletionService<Object> executorService;
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
    
    public SegmentedList(ExecutorCompletionService<Object> executorService, Function<Range<Long>, ClosableIterator<T>> itemSupplier, Range<Long> cacheRange, RangeCostModel costModel) {
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
            
            // Find the range between the first and the last gap
            Range<Long> requestRange = null;
            for(RangeInfo<T> ri : rangeInfos) {
                
            }
            
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


/**
 * Performs cache lookup
 * @author raven
 *
 */
class QueryExecutionFactorySegmentedCache {

//    QueryExecution createQueryExecution(Query query) {
//        
//        
//        Function<Range<Long>, ClosableIterator<T>> itemSupplier        
//    }
}

class QueryExecutionSegmentedCache {
    protected Query query;    
    
    protected Map<Var, Var> varMap; // Mapping to rename variables of the bindings in the cache
    //protected SegmentedList<Binding> listCache;
    protected Function<Range<Long>, ClosableIterator<Binding>> bindingSupplier;

    protected Range<Long> range;
    protected List<String> varNames;
    
    protected transient ClosableIterator<Binding> it;
    
    public ResultSet execSelect() {
        it = bindingSupplier.apply(range);
        
        Iterator<Binding> i = it;
        if(varMap != null) {
            Iterable<Binding> tmp = () -> it;
            i = StreamSupport.stream(tmp.spliterator(), false)
                .map(b -> BindingUtils.rename(b, varMap))
                .iterator();
        }        
        
        ResultSet result = ResultSetUtils.create(varNames, i);
        return result;
        
    }
    
    public void close() {
        if(it != null) {
            it.close();
        }
    }
}




