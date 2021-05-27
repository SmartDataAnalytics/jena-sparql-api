package org.aksw.commons.rx.cache.range;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * An extension of loading cache that allows for making explicit
 * references to cached entries such that they won't be evicted.
 * 
 * As long as an entry's reference is not released the cache entry will not be evicted.
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class NavigableClamingCache<C extends Comparable<C>, V> {
	protected Cache<C, V> cache;
	protected NavigableSet<C> cacheKeys;
	
	protected NavigableMap<C, Ref<V>> claimed;

	// protected BiConsumer<Range<C>> insertListener;
	
	public NavigableClamingCache(CacheBuilder<C, V> cacheBuilder) {
		cacheBuilder
			.removalListener(ev -> {
				synchronized (this) {
					cacheKeys.remove(ev.getKey());
				}
			});
	}
	
	public void put(C key, V value) {		
		synchronized (this) {
			cacheKeys.add(key);
			cache.put(key, value);
		}
	}

	/** Claim all items in the given request range */
	public NavigableMap<C, Ref<V>> claimAll(Range<C> requestRange) {
		NavigableMap<C, Ref<V>> result = new TreeMap<>();
		synchronized (this) {	
			// Claim those items that are already claimed anyway 
			NavigableMap<C, Ref<V>> tmp = filterByRange(claimed, requestRange);
			for (Entry<C, Ref<V>> entry : tmp.entrySet()) {
				
				// Acquire another reference
				result.put(entry.getKey(), entry.getValue().acquire());
			}

			// Check for any unclaimed items provided by the cache
			// These items are then added to the claimed map
			Set<C> resultKeySet = result.keySet();

			Set<C> keys = filterByRange(cacheKeys, requestRange);
			for (C key : keys) {
				
				if (!resultKeySet.contains(key)) {					
					V value = cache.getIfPresent(key);

					if (value != null) {
						// Note: The removal listener of RefImpl is synchronized on 'this'!
						Ref<V> rootRef = RefImpl.create(value, this, () -> {
							// Hand back the value to the cache
							cache.put(key, value);
							claimed.remove(key);
						});
						
						result.put(key, rootRef.acquire());	
						rootRef.close();
						claimed.put(key, rootRef);
					}
				}
			}
		}
		
		return result;
	}
	
	/** Create a set view over a navigable set with items filtered to the given range */
	public static <T extends Comparable<T>> NavigableSet<T> filterByRange(NavigableSet<T> set, Range<T> range) {
		NavigableSet<T> result = set;
	
		if (range.hasLowerBound()) {
			T endpoint = range.lowerEndpoint();
			boolean isInclusive = range.lowerBoundType().equals(BoundType.CLOSED);
			result = result.tailSet(endpoint, isInclusive);
		}
		
		if (range.hasUpperBound()) {
			T endpoint = range.upperEndpoint();
			boolean isInclusive = range.upperBoundType().equals(BoundType.CLOSED);
			result = result.headSet(endpoint, isInclusive);
		}
		
		return result;
	}
	
	/** Create a set view over a navigable set with items filtered to the given range */
	public static <K extends Comparable<K>, V> NavigableMap<K, V> filterByRange(NavigableMap<K, V> map, Range<K> range) {
		NavigableMap<K, V> result = map;
	
		if (range.hasLowerBound()) {
			K endpoint = range.lowerEndpoint();
			boolean isInclusive = range.lowerBoundType().equals(BoundType.CLOSED);
			result = result.tailMap(endpoint, isInclusive);
		}
		
		if (range.hasUpperBound()) {
			K endpoint = range.upperEndpoint();
			boolean isInclusive = range.upperBoundType().equals(BoundType.CLOSED);
			result = result.headMap(endpoint, isInclusive);
		}
		
		return result;
	}
}