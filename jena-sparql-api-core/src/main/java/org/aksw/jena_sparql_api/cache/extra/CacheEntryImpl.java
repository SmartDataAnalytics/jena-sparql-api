package org.aksw.jena_sparql_api.cache.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/2/11
 *         Time: 11:47 AM
 */
public class CacheEntryH2
    extends CacheEntryBase
{
    private String queryString;
    private String queryHash;

    public CacheEntryH2(long timestamp, long lifespan, InputStreamProvider inputStreamProvider, String queryString, String queryHash) {
        super(timestamp, lifespan, inputStreamProvider);
        this.queryString = queryString;
        this.queryHash = queryHash;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getQueryHash() {
        return queryHash;
    }
}
