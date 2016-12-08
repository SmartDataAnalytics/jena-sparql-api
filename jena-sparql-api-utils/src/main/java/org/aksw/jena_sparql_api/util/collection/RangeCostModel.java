package org.aksw.jena_sparql_api.util.collection;

import java.util.function.Function;

import com.google.common.collect.Range;

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


public class RangeCostModel {

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