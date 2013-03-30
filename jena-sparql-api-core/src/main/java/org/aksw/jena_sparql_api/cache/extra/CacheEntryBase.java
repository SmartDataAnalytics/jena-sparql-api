package org.aksw.jena_sparql_api.cache.extra;

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
    private InputStreamProvider inputStreamProvider;

    public CacheEntryBase(long timestamp, long lifespan, InputStreamProvider inputStreamProvider) {
        this.timestamp = timestamp;
        this.lifespan = lifespan;
        this.inputStreamProvider = inputStreamProvider;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLifespan() {
        return lifespan;
    }

    public InputStreamProvider getInputStreamProvider() {
        return inputStreamProvider;
    }

}