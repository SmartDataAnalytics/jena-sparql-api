package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.Connection;

import org.aksw.jena_sparql_api.cache.extra.CacheEntry;

public interface CacheBackendDao {
    
    /**
     * The lookup returns an InputStream + metadata
     * 
     *  
     * @param conn
     * @param service
     * @param queryString
     * @param closeConn Whether to close the connection when the inputStream is closed or consumed
     * @return
     * @throws Exception
     */
	CacheEntry lookup(Connection conn, String service, String queryString, boolean closeConn) throws Exception;
	void write(Connection conn, String service, String queryString, InputStream in) throws Exception;
}