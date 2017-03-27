package org.aksw.jena_sparql_api.cache.staging;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.aksw.jena_sparql_api.cache.extra.CacheEntryImpl;
import org.apache.commons.io.IOUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheBackendMem
    implements CacheBackend
{
    private Cache<String, byte[]> hashToCache;

    public CacheBackendMem() {
        this(CacheBuilder.newBuilder()
                .build());
    }

    public CacheBackendMem(Cache<String, byte[]> hashToCache) {
        super();
        this.hashToCache = hashToCache;
    }

    @Override
    public CacheEntry lookup(String service, String queryString) {
        String key = service + "-" + queryString;
        byte[] data = hashToCache.getIfPresent(key);
        CacheEntry result;
        if(data == null) {
            result = null;
        } else {
            InputStream in = new ByteArrayInputStream(data);
            // TODO Properly deal with time to live
            result = new CacheEntryImpl(System.currentTimeMillis(), 1000, in, queryString, key);
        }

        return result;
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
        try {
            byte[] bytes = IOUtils.toByteArray(in);
            hashToCache.put(service + "-" + queryString, bytes);

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
