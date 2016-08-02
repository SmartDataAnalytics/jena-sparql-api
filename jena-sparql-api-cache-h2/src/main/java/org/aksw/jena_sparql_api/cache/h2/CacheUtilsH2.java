package org.aksw.jena_sparql_api.cache.h2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendDao;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendDaoPostgres;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendDataSource;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

/**
 * Utils for generating a cache
 *
 * @author Dimitris Kontokostas
 * @since 7/1/14 4:59 PM
 */
public class CacheUtilsH2 {

    /**
     * Create a cache frontend based on H2 database
     *
     * @param dbName the name of the DB
     * @param dbInMemory true creates this database in memory, otherwise in a file
     * @param cacheTTL the Time-To-Live for the cache
     * @return an initialized CacheFrontend or throws an a RuntimeException in case of error
     * @throws RuntimeException in case of error
     */
    public static CacheFrontend createCacheFrontend(String dbName, boolean dbInMemory, long cacheTTL) {
        CacheFrontend result = createCacheFrontend(null, dbName, dbInMemory, cacheTTL);
        return result;
    }

    public static CacheFrontend createCacheFrontend(String dbDir, String dbName, boolean dbInMemory, long cacheTTL) {

        String dbType = "file";
        if (dbInMemory) {
            dbType = "mem:";
        }

        String dbd = StringUtils.isEmpty(dbDir) ? "" : dbDir + "/";

        List<String> options = new ArrayList<String>();

        if(!dbInMemory) {
            options.add("AUTO_SERVER=TRUE");
        }

        options.add("AUTO_RECONNECT=TRUE");
        options.add("DB_CLOSE_DELAY=-1");

        String optionsStr = options.stream().collect(Collectors.joining(";"));

        try {
            Class.forName("org.h2.Driver");
            //jdbc:h2:" + dbDir + "/"

            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:" + dbType + ":" + dbd + dbName + ";" + optionsStr);
            dataSource.setUser("sa");
            dataSource.setPassword("sa");

            String schemaResourceName = "/org/aksw/jena_sparql_api/cache/cache-schema-pgsql.sql";
            InputStream in = CacheBackendDao.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }

            InputStreamReader reader = new InputStreamReader(in);
            Connection conn = dataSource.getConnection();
            try {
                RunScript.execute(conn, reader);
            } finally {
                conn.close();
            }

            CacheBackendDao dao = new CacheBackendDaoPostgres(cacheTTL);
            CacheBackend cacheBackend = new CacheBackendDataSource(dataSource, dao);

            return new CacheFrontendImpl(cacheBackend);

        } catch (Exception e) {
            throw new RuntimeException("Cannot create H2 CacheFrontend", e);
        }
    }

    /**
     * Create a QueryExecutionFactory that uses an H2 database as a cache
     *
     * @param decoratee the QueryExecutionFactory we want to decorate
     * @param dbName the name of the DB
     * @param dbInMemory true creates this database in memory, otherwise in a file
     * @param cacheTTL the Time-To-Live for the cache
     * @return an initialized CacheFrontend or throws an a RuntimeException in case of error
     * @throws RuntimeException in case of error
     */
    public static QueryExecutionFactory createQueryExecutionFactory(QueryExecutionFactory decoratee, String dbName, boolean dbInMemory, long cacheTTL) {
        CacheFrontend cacheFrontend = createCacheFrontend(dbName, dbInMemory, cacheTTL);
        return new QueryExecutionFactoryCacheEx(decoratee, cacheFrontend);
    }
}
