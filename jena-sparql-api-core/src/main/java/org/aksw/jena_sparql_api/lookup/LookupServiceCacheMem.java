package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.jena.ext.com.google.common.cache.CacheBuilder;

import com.google.common.cache.Cache;

import io.reactivex.Flowable;

public class LookupServiceCacheMem<K, V>
    implements LookupService<K, V>
{
	// A cache; CompletableFuture allows for pending values
    private Cache<K, CompletableFuture<V>> cache;//new LRUMap<K, V>();

    private LookupService<K, V> base;

    public LookupServiceCacheMem(LookupService<K, V> base) {
        this(base, 10000);
    }

    public LookupServiceCacheMem(LookupService<K, V> base, long maxCacheSize) {
        this(base, CacheBuilder.newBuilder().maximumSize(maxCacheSize).build());
    }

    public LookupServiceCacheMem(LookupService<K, V> base, Cache<K, CompletableFuture<V>> cache) {
        this.base = base;
        this.cache = cache;
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> keys) {
        Map<K, V> map = new HashMap<K, V>();

    	
        Set<K> open = new HashSet<K>();

        for(K key : keys) {
            CompletableFuture<V> v = cache.getIfPresent(key);
            // Whenever a future finishes, trigger onNext
            
                V v = cache.get(key);

                map.put(key, v);
            } else {
                open.add(key);
            }
        }

//    	Flowable.concat(
//        Flowable<Entry<K, V>> tmp = Flowable.fromIterable(map.entrySet());

        
        Flowable<Entry<K, V>> requestPart = base.apply(open).subscribe(remaining -> {
            K k = remaining.getKey();
            V v = remaining.getValue();
        	cache.putAll(remaining);
            result.putAll(remaining);

            return result;
        });

        Flowable.conc
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

    public static <K, V> LookupServiceCacheMem<K, V> create(LookupService<K, V> base, Map<K, V> cache) {
        LookupServiceCacheMem<K, V> result = new LookupServiceCacheMem<K, V>(base, cache);
        return result;
    }
}
