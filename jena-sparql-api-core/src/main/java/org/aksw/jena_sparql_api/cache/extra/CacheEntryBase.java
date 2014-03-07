package org.aksw.jena_sparql_api.cache.extra;

import java.io.InputStream;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/2/11
 *         Time: 11:48 AM
 */
public class CacheEntryBase
    implements CacheEntry
{
    private long timestamp;
    private long lifespan;
    private InputStream inputStream;

    public CacheEntryBase(long timestamp, long lifespan, InputStream inputStream) {
        this.timestamp = timestamp;
        this.lifespan = lifespan;
        this.inputStream = inputStream;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLifespan() {
        return lifespan;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}