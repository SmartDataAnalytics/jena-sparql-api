package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import io.reactivex.Flowable;


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
    protected Cache<K, V> cache;//new LRUMap<K, V>();
    private LookupService<K, V> base;

    
    
    public LookupServiceCacheMem(LookupService<K, V> base) {
        this(base, 10000);
    }

    public LookupServiceCacheMem(LookupService<K, V> base, long maxCacheSize) {
        this(base, CacheBuilder.newBuilder().maximumSize(maxCacheSize).build());
    }

    public LookupServiceCacheMem(LookupService<K, V> base, Cache<K, V> cache) {
        this.base = base;
        this.cache = cache;
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> keys) {
    	Map<K, V> cachedEntries = cache.getAllPresent(keys);
    	Set<K> remaining = Sets.difference(Sets.newHashSet(keys), cachedEntries.keySet());

    	Flowable<Entry<K, V>> result = Flowable
    			.fromIterable(cachedEntries.entrySet())
    			.concatWith(base.apply(remaining));

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
