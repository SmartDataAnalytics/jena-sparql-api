package org.aksw.jena_sparql_api.cache.extra;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple interface for Data Access Objects that are based on JDBC connection
 * objects.
 *
 * @author Claus Stadler
 */
public interface SqlDao
{
	/**
	 * Set the JDBC connection object to be used by the DAO.
	 * 
	 */
	Connection getConnection();

	/**
	 * Retrieve the JDBC connection object that is currently associated with
	 * this DAO.
	 *
	 * @return The JDBC connection object currently associated with this DAO.
	 */
	void setConnection(Connection conn)
		throws SQLException;
}
