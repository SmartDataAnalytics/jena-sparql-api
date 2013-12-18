package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.Connection;

import org.aksw.jena_sparql_api.cache.extra.CacheEntry;

public interface CacheBackendDao {
	CacheEntry lookup(Connection conn, String service, String queryString) throws Exception;
	void write(Connection conn, String service, String queryString, InputStream in) throws Exception;
}