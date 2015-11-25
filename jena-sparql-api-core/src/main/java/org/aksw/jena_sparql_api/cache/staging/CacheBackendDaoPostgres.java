package org.aksw.jena_sparql_api.cache.staging;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheEntryImpl;
import org.aksw.jena_sparql_api.cache.extra.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * 
 * @author raven
 *
 */
public class CacheBackendDaoPostgres
    implements CacheBackendDao
{	
	private static final Logger logger = LoggerFactory.getLogger(CacheBackendDaoPostgres.class);

	private boolean validateHash = true;
	private long timeToLive;

	
	private String QUERY_LOOKUP = "SELECT * FROM \"query_cache\" WHERE \"id\" = ?";
	private String QUERY_INSERT = "INSERT INTO \"query_cache\"(\"id\", \"query_string\", \"data\", \"time_of_insertion\", \"hit_count\") VALUES (?, ?, ?, ?, ?)";
	private String QUERY_UPDATE = "UPDATE \"query_cache\" SET \"data\"=?, \"time_of_insertion\" = ? WHERE \"id\" = ?";

	public CacheBackendDaoPostgres() {
	    this(24l * 60l * 60l * 1000l);
	}

	public CacheBackendDaoPostgres(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * @param closeConn Whether to COMMIT(!) AND CLOSE the connection after consuming or closing the result of a lookup operation
	 */
	@Override
	public CacheEntryImpl lookup(final Connection conn, String service, String queryString, final boolean closeConn) throws SQLException
	{		
		String md5 = StringUtils.md5Hash(createHashRoot(service, queryString));
		// String md5 = StringUtils.md5Hash(queryString);
		
		//final ResultSet rs = SqlUtils.executeCore(conn, QUERY_LOOKUP, md5);

		Object[] args = {md5};
		String sql = QUERY_LOOKUP;
		logger.trace("Executing statement '" + sql + "' with args " + Arrays.asList(args));

        final PreparedStatement stmt = conn.prepareStatement(sql);

        SqlUtils.executeSetArgs(stmt, args);
        final ResultSet rs = stmt.executeQuery();

		
		
		/*
		CacheCoreIterator it = new CacheCoreIterator(rs, new IClosable() {
			@Override
			public void close() {
	            SqlUtils.close(rs);
	            if(closeConn) {
	                System.out.println("ConnectionWatch Closed (lookup) " + conn);
	                SqlUtils.close(conn);
	            }
			}
		});
		*/
		

        IClosable closeAction = new IClosable() {
            boolean isClosed = false;
            
            @Override
            public void close() {
                if(!isClosed) {
                    SqlUtils.close(rs);
                    SqlUtils.close(stmt);
                    
                    if(closeConn) {
                        //System.out.println("ConnectionWatch Closed (lookup) " + conn);
                        try {
                            conn.commit();
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                        SqlUtils.close(conn);
                    }
                }
                
                isClosed = true;
            }
        };

		CacheEntryImpl result = null;
		if(rs.next()) {
			result = CacheCoreIterator.createCacheEntry(rs, closeAction, timeToLive);
			
			if (validateHash) {
				String cachedQueryString = result.getQueryString();

				if (!cachedQueryString.equals(queryString)) {
					logger.error("HASH-CLASH:\n" + "Service: " + service
							+ "\nNew QueryString: " + queryString
							+ "\nOld QueryString: " + cachedQueryString);
					throw new RuntimeException("Hash-Clash - Man you're lucky"); 
					//it.close();
					//return null;
				}
			}

			/*
			if(it.hasNext()) {
				// NOTE Can't happen anymore, as the md5 hash is now the primary key
				// In the old version we had an auto increment column
				logger.warn("Multiple cache hits found, just one expected.");
			}
			*/		
		}
		else {
		    closeAction.close();
		}

//		if(result != null) {
//		    //logger.info("Cache hit for " + queryString);
//		    System.out.println("Cache hit for " + queryString);
//		} else {
//		    System.out.println("Cache miss for " + queryString);
//		}
		
		return result;
	}


	@Override
	public void write(Connection conn, String service, String queryString, InputStream in) throws SQLException, IOException
	{
		String md5 = StringUtils.md5Hash(createHashRoot(service, queryString));

		// Issue: The lookup would close the connection, although we still need the connection to perform
		// the write
		CacheEntryImpl entry = lookup(conn, service, queryString, false);
		boolean doesEntryExist = entry != null;
		
		if(doesEntryExist) {
		    // Close the input stream associated with the entry
		    entry.getInputStream().close();
		}
		
		Timestamp timestamp = new Timestamp(new GregorianCalendar().getTimeInMillis());

		//rs = executeQuery(Query.LOOKUP, md5);

		
		// Unfortunately we have to read the whole string into memory, as the postgres driver otherwise raises
		// an exception that writing from an InputStream into the database is not supported :(
		String hack;
		try {
            hack = StreamUtils.toString(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
		// hack should be the InputStream 'in'
		if(doesEntryExist) {
			SqlUtils.execute(conn, QUERY_UPDATE, Void.class, hack, timestamp, md5);
		} else {
			SqlUtils.execute(conn, QUERY_INSERT, Void.class, md5, queryString, hack, timestamp, 1);
		}
		// return lookup(queryString);
	}

	public String createHashRoot(String service, String queryString) {
		return service + " " + queryString;
	}

}