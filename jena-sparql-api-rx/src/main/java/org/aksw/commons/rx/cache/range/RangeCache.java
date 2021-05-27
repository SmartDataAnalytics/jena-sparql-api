package org.aksw.commons.rx.cache.range;

import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;

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
public class RangeCache<C extends Comparable<C>, V> {
	protected Cache<Range<C>, V> cache;
	protected RangeSet<C> cacheKeys;
	
	protected RangeMap<C, Ref<V>> claimed;

	// protected BiConsumer<Range<C>> insertListener;
	
	public RangeCache(CacheBuilder<Range<C>, V> cacheBuilder) {
		cacheBuilder
			.removalListener(ev -> {
				synchronized (this) {
					cacheKeys.remove(ev.getKey());
				}
			});
	}

	public void put(Range<C> key, V value) {		
		synchronized (this) {
			// Ensure there is no overlap
			if (cacheKeys.intersects(key)) {
				throw new IllegalArgumentException(String.format("Provided range %s overlaps with an existing entry", key));
			}

			cacheKeys.add(key);
			cache.put(key, value);
		}
	}

	/** Claim all items in the given request range */
	public RangeMap<C, Ref<V>> claimAll(Range<C> requestRange) {
		RangeMap<C, Ref<V>> result = TreeRangeMap.create();
		synchronized (this) {	
			// Claim those items that are already claimed anyway 
			RangeMap<C, Ref<V>> tmp = claimed.subRangeMap(requestRange);
			for (Entry<Range<C>, Ref<V>> entry : tmp.asMapOfRanges().entrySet()) {
				
				// Acquire another reference
				result.put(entry.getKey(), entry.getValue().acquire());
			}

			// Check for any unclaimed items provided by the cache
			// These items are then added to the claimed map
			Set<Range<C>> resultKeySet = result.asMapOfRanges().keySet();

			RangeSet<C> keys = cacheKeys.subRangeSet(requestRange);
			for (Range<C> key : keys.asRanges()) {
				
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

}