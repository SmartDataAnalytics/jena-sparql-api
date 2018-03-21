package org.aksw.jena_sparql_api.cache.extra;


import java.io.InputStream;

/**
 * An interface similar to CacheCore, except that an additional service argument is supported.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 10:55 PM
 */
public interface CacheBackend {
    CacheEntry lookup(String service, String queryString);
    void write(String service, String queryString, InputStream in);
    
    boolean isReadOnly();
}
