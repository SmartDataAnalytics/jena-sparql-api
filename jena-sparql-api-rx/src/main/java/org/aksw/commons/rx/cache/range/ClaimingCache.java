package org.aksw.commons.rx.cache.range;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;

import com.google.common.cache.LoadingCache;

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
public class ClaimingCache<K, V> {
	protected LoadingCache<K, V> cache;
	protected Map<K, Ref<V>> claimed;

//	/** Reusing the RemovalListener interface */
//	protected Set<RemovalListener<K, V>> loadingListeners = Sets.newIdentityHashSet();;
//	
//	public void addLoadListener(RemovalListener<K, V> listener) {
//		CacheBuilder.newBuilder().lis
//	}
	
	public ClaimingCache(LoadingCache<K, V> cache) {
		this(cache, new HashMap<>());
	}
	
	public ClaimingCache(LoadingCache<K, V> cache, Map<K, Ref<V>> claimed) {
		super();
		this.cache = cache;
		this.claimed = claimed;
	}
	
	/**
	 * Claim a reference to the key's entry.
	 * 
	 * @param key
	 * @return
	 * @throws ExecutionException
	 */
	public Ref<V> claim(K key) throws ExecutionException {
		Ref<V> result = null;
		Ref<V> rootRef;
 
		// Synchronize on 'claimed' because removals can occur asynchronously
		synchronized (claimed) {
			rootRef = claimed.get(key);

			if (rootRef != null) {
				result = rootRef.acquire(null);
			}
		}
		
		if (rootRef == null) {
			// Don't block 'claimed' while computing the value
			// Hence, compute the value outside of the synchronized block
			V v = cache.get(key);

			// Put a reference to the value into claimed
			// (if that hasn't happened asynchronously already)
			synchronized (claimed) {
				// Check whether there is a value in the cache
				rootRef = claimed.get(key);
				if (rootRef == null) {
					// Note that the root ref is synchronized on 'claimed' as well
					// Hence, if the ref had been released then claimed.get(key) would have yeld null
					rootRef = RefImpl.create(v, claimed, () -> {
						// Hand back the value to the cache
						cache.put(key, v);
						claimed.remove(key);
					}, null);
					result = rootRef.acquire(null);
					rootRef.close();
	
					claimed.put(key, rootRef);
				} else {
					result = rootRef.acquire(null);
				}
			}
		}
		
		return result;
	}	
}