package org.aksw.jena_sparql_api.cache.extra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Claus Stadler
 *
 *         Date: 7/26/11
 *         Time: 3:23 PM
 */
public class SqlUtils {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

	public static <T> void executeSetArgs(PreparedStatement stmt, Object ...args)
		throws SQLException
	{
		for(int i = 0; i < args.length; ++i) {
		    int index = i + 1;
		    Object arg = args[i];
		    
		    if(arg instanceof InputStream) {
		        // A hack because of some Postgres drivers not capable of dealing with input streams
		        // TODO Only use the hack as a fallback
		        
		        
		        InputStream in = (InputStream)arg;
		        //stmt.setBinaryStream(index, (InputStream)in);
		        
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        try {
                    StreamUtils.copyThenClose(in, out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
		        
		        byte[] buf = out.toByteArray();
		        ByteArrayInputStream in2 = new ByteArrayInputStream(buf);
		        
		        //stmt.setBytes(index, out.toByteArray());
		        //stmt.setBlob(index, in2, buf.length);
		        
		        stmt.setBinaryStream(index, in2, buf.length);
		        
		    } else {
		        stmt.setObject(index, arg);
		    }
		}

		// Pad with nulls
		int n = stmt.getParameterMetaData().getParameterCount();
		//System.out.println("x = " + n);
		for(int i = args.length; i < n; ++i) {
			stmt.setObject(i + 1, null);
		}
		//System.out.println("y = " + n);
	}

	/* Closing the statements also closes the resultset...
	public static ResultSet execute(Connection conn, String sql)
		throws SQLException
	{
		ResultSet result = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
		}
		finally {
			if(stmt != null) {
				stmt.close();
			}
		}

		return result;
	}
	*/


	public static <T> T execute(Connection conn, String sql, Class<T> clazz, Object ...args)
		throws SQLException
	{
		PreparedStatement stmt = conn.prepareStatement(sql);

		T result = execute(stmt, clazz, args);

		stmt.close();

		return result;
	}

	public static ResultSet executeCore(Connection conn, String sql, Object ...args)
		throws SQLException
	{
		logger.trace("Executing statement '" + sql + "' with args " + Arrays.asList(args));

		PreparedStatement stmt = conn.prepareStatement(sql);

		executeSetArgs(stmt, args);
		ResultSet result = stmt.executeQuery();

		//stmt.close();

		return result;
	}

	public static ResultSet execute(PreparedStatement stmt, Object ...args)
		throws SQLException
	{
		executeSetArgs(stmt, args);

		ResultSet result = stmt.executeQuery();
		return result;
	}

	public static <T> T execute(PreparedStatement stmt, Class<T> clazz, Object ...args)
		throws SQLException
	{
		executeSetArgs(stmt, args);

		T result = null;
		if(clazz == null || Void.class.equals(clazz)) {
			stmt.execute();
		}
		else {
			ResultSet rs = stmt.executeQuery();
			result = SqlUtils.single(rs, clazz);
			rs.close();
		}

		return result;
	}

	public static <T> List<T> executeList(Connection conn, String sql, Class<T> clazz, Object ...args)
		throws SQLException
	{
		logger.trace("Executing statement '" + sql + "' with args " + Arrays.asList(args));

		PreparedStatement stmt = conn.prepareStatement(sql);

		List<T> result = executeList(stmt, clazz, args);

		stmt.close();

		return result;
	}

	public static <T> List<T> executeList(PreparedStatement stmt, Class<T> clazz, Object ...args)
		throws SQLException
	{
		executeSetArgs(stmt, args);

		ResultSet rs = stmt.executeQuery();
		List<T> result = SqlUtils.list(rs, clazz);

		return result;
	}

	
	public static void close(Connection conn) {
        if(conn != null) {
            try {
				conn.close();
		    }
            catch(SQLException e) {
		        logger.error("Failed to close connection " + conn, e);
		    }
        }
	}

	public static void close(Statement stmt)
    {
        if(stmt != null) {
            try {
                stmt.close();
            }
            catch(SQLException e) {
                logger.error("Failed to close statement " + stmt, e);
            }
        }
    }
	
	public static void close(ResultSet resultSet)
	{
        if(resultSet != null) {
    		try {
    		    resultSet.close();
    		}
    		catch(SQLException e) {
    			logger.error("Failed to close result set " + resultSet, e);
    		}
	    }
	}


    public static <T> T single(ResultSet rs, Class<T> clazz)
        throws SQLException
    {
        return single(rs, clazz, true);
    }


    /**
     * Returns the 1st column of the first row or null of there is no row.
     * Also throws exception if there is more than 1 row and 1 column.
     *
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static <T> T single(ResultSet rs, Class<T> clazz, boolean bClose)
        throws SQLException
    {
        if(rs.getMetaData().getColumnCount() != 1)
            throw new RuntimeException("only a single column expected");

        T result = null;

        if(rs.next()) {
            Object o = rs.getObject(1);;
            //System.out.println("Result = " + o);
            result = (T)o;

            if(rs.next())
                throw new RuntimeException("only at most 1 row expected");
        }

        if(bClose)
            rs.close();

        return result;
    }


    public static <T> List<T> list(ResultSet rs, Class<T> clazz)
        throws SQLException
    {
        return list(rs, clazz, true);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> list(ResultSet rs, Class<T> clazz, boolean bClose)
        throws SQLException
    {
        List<T> result = new ArrayList<T>();

        try {
            while(rs.next()) {
                Object o = rs.getObject(1);
                //System.out.println("Result = " + o);
                T item = (T)o;
                result.add(item);
            }
        }
        finally {
            if(bClose)
                rs.close();
        }

        return result;

    }
}
