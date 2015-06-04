package org.aksw.jena_sparql_api.lookup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.map.LRUMap;

public class LookupServiceCacheMem<K, V>
    implements LookupService<K, V>
{
    private Map<K, V> cache = new LRUMap<K, V>();

    private LookupService<K, V> base;

    public LookupServiceCacheMem(LookupService<K, V> base) {
        this(base, 1000);
    }

    public LookupServiceCacheMem(LookupService<K, V> base, int maxCacheSize) {
        this(base, new LRUMap<K, V>(maxCacheSize));
    }

    public LookupServiceCacheMem(LookupService<K, V> base, Map<K, V> cache) {
        this.base = base;
        this.cache = cache;
    }

    @Override
    public Map<K, V> apply(Iterable<K> keys) {
        Map<K, V> result = new HashMap<K, V>();

        Set<K> open = new HashSet<K>();

        for(K key : keys) {
            if(cache.containsKey(key)) {
                V v = cache.get(key);

                result.put(key, v);
            } else {
                open.add(key);
            }
        }

        Map<K, V> remaining = base.apply(open);

        cache.putAll(remaining);
        result.putAll(remaining);

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
