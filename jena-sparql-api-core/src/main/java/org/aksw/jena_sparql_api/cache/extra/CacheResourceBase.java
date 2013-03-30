package org.aksw.jena_sparql_api.cache.extra;

/**
 * CURRENTLY NOT USED
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 3:46 PM
 */
public abstract class CacheResourceBase
    implements CacheResource
{
    private long timestamp;
    private long lifespan;

    public CacheResourceBase(long timestamp, long lifespan) {
        this.timestamp = timestamp;
        this.lifespan = lifespan;
    }

    public boolean isOutdated() {
        return System.currentTimeMillis() - timestamp > lifespan;
    }
}
