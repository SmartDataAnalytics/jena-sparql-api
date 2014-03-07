package org.aksw.jena_sparql_api.cache.extra;

import java.io.InputStream;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/2/11
 *         Time: 11:47 AM
 */
public class CacheEntryImpl
    extends CacheEntryBase
{
    private String queryString;
    private String queryHash;

    //private List<ICloseable> closeActions = new ArrayList<>
    
    public CacheEntryImpl(long timestamp, long lifespan, InputStream inputStream, String queryString, String queryHash) {
        super(timestamp, lifespan, inputStream);
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
