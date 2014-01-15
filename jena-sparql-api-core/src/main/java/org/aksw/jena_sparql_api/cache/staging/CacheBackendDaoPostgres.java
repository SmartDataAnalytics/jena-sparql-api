package org.aksw.jena_sparql_api.cache.staging;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.SqlUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheEntryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class CloseActionCollection
	implements IClosable
{
	private Collection<IClosable> closables;
	
	public CloseActionCollection() {
		closables = new ArrayList<IClosable>();
	}
	
	public CloseActionCollection(Collection<IClosable> closables) {
		this.closables = closables; 
	}
	
	public Collection<IClosable> getClosables() {
		return this.closables;
	}

	@Override
	public void close() {
		List<Exception> exceptions = null;
		for(IClosable closable: closables) {
			try {
				closable.close();
			} catch(Exception e) {
				if(exceptions == null) {
					exceptions = new ArrayList<Exception>();
					exceptions.add(e);
				}
			}
		}
		
		if(exceptions != null) {
			throw new RuntimeException(exceptions.size() + " exceptions thrown." + exceptions);
		}		
	}
}


//enum Transaction {
//	Nothing,
//	Commit,
//	Rollback,
//}
//
//class CloseActionConnection
//	implements IClosable
//{
//	private Connection conn;
//	
//	public CloseActionConnection(Connection conn) {
//		
//	}
//}


public class CacheBackendDaoPostgres
    implements CacheBackendDao
{	
	private static final Logger logger = LoggerFactory.getLogger(CacheBackendDaoPostgres.class);

	private boolean validateHash = true;

	
	private String QUERY_LOOKUP = "SELECT * FROM \"sparql_query_cache\" WHERE \"id\" = ? LIMIT 2";
	private String QUERY_INSERT = "INSERT INTO \"sparql_query_cache\"(\"id\", \"query_string\", \"data\", \"time_of_insertion\") VALUES (?, ?, ?, ?)";
	private String QUERY_UPDATE = "UPDATE \"query_cache\" SET \"data\"=?, \"time\" = ? WHERE \"id\" = ?";


	/**
	 */
	@Override
	public CacheEntryImpl lookup(Connection conn, String service, String queryString) throws SQLException
	{		
		String md5 = StringUtils.md5Hash(createHashRoot(service, queryString));
		// String md5 = StringUtils.md5Hash(queryString);

		final ResultSet rs = SqlUtils.executeCore(conn, QUERY_LOOKUP, md5);

		CacheCoreIterator it = new CacheCoreIterator(rs, new IClosable() {
			@Override
			public void close() {
				SqlUtils.close(rs);
			}
		});
		
		CacheEntryImpl result = null;		
		if(it.hasNext()) {
			result = it.next();
			
			if (validateHash) {
				String cachedQueryString = result.getQueryString();

				if (!cachedQueryString.equals(queryString)) {
					logger.error("HASH-CLASH:\n" + "Service: " + service
							+ "\nNew QueryString: " + queryString
							+ "\nOld QueryString: " + cachedQueryString);
					return null;
				}
			}

			if(it.hasNext()) {
				// NOTE Can't happen anymore, as the md5 hash is now the primary key
				// In the old version we had an auto increment column
				logger.warn("Multiple cache hits found, just one expected.");
			}		
		}	
			
		return result;
	}


	@Override
	public void write(Connection conn, String service, String queryString, InputStream in) throws SQLException
	{
		String md5 = StringUtils.md5Hash(createHashRoot(service, queryString));

		CacheEntryImpl entry = lookup(conn, service, queryString);

		Timestamp timestamp = new Timestamp(new GregorianCalendar().getTimeInMillis());

		//rs = executeQuery(Query.LOOKUP, md5);

		if (entry != null) {
			SqlUtils.execute(conn, QUERY_UPDATE, Void.class, in, timestamp, md5);
		} else {
			SqlUtils.execute(conn, QUERY_INSERT, Void.class, md5, queryString, in, timestamp);
		}
		// return lookup(queryString);
	}

	public String createHashRoot(String service, String queryString) {
		return service + " " + queryString;
	}

}