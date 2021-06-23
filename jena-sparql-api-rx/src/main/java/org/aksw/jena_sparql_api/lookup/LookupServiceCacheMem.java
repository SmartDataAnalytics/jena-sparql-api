package org.aksw.jena_sparql_api.lookup;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import io.reactivex.rxjava3.core.Flowable;


/**
 * A cache for <b>already retrieved</b> values. This is not a cache for requests.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class LookupServiceCacheMem<K, V>
    implements LookupService<K, V>
{
    protected Cache<K, Optional<V>> cache;//new LRUMap<K, V>();
    protected Cache<K, Boolean> missCache;

    private LookupService<K, V> base;

    public LookupServiceCacheMem(LookupService<K, V> base) {
        this(base, 10000);
    }

    public LookupServiceCacheMem(LookupService<K, V> base, long maxCacheSize) {
        this(base,
            CacheBuilder.newBuilder().maximumSize(maxCacheSize).build(),
            CacheBuilder.newBuilder().maximumSize(maxCacheSize).build()
        );
    }

    public LookupServiceCacheMem(LookupService<K, V> base, Cache<K, Optional<V>> cache, Cache<K, Boolean> missCache) {
        this.base = base;
        this.cache = cache;
        this.missCache = missCache;
    }

    @Override
    public Map<K, V> fetchMap(Iterable<K> keys) {
        Set<K> lookupKeys = Sets.newLinkedHashSet(keys);

        Map<K, Optional<V>> cachedEntries = cache.getAllPresent(keys);
        lookupKeys.removeAll(cachedEntries.keySet());

        Set<K> knownMissKeys = new LinkedHashSet<>(Sets.intersection(lookupKeys, missCache.asMap().keySet()));
        lookupKeys.removeAll(knownMissKeys);

//		System.out.println("Remaining size: " + lookupKeys.size() + " cache size: " + cache.size());

        Map<K, V> fetchedMap = base.fetchMap(lookupKeys);

        Map<K, V> result = new LinkedHashMap<>();
        for(K key : keys) {
            V value;
            if(cachedEntries.containsKey(key)) {
                value = cachedEntries.get(key).orElse(null);
                cache.put(key, Optional.ofNullable(value));
                result.put(key, value);
            } else if(fetchedMap.containsKey(key)) {
                value = fetchedMap.get(key);
                cache.put(key, Optional.ofNullable(value));
                result.put(key, value);
            } else {
                missCache.put(key, true);
            }
        }

        return result;
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> keys) {
        Map<K, V> map = fetchMap(keys);
        Flowable<Entry<K, V>> result = Flowable.fromIterable(map.entrySet());
        return result;
    }

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base);
        return result;
    }

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, int maxCacheSize) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, maxCacheSize);
        return result;
    }

//    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, Map<K, V> cache) {
//        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, cache);
//        return result;
//    }
}
