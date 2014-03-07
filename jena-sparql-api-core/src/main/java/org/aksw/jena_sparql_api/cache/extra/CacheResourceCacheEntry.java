package org.aksw.jena_sparql_api.cache.extra;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 3:46 PM
 */
public class CacheResourceCacheEntry
    implements CacheResource
{
    private static Logger logger = LoggerFactory.getLogger(CacheResourceCacheEntry.class);

    private CacheEntry cacheEntry;

    public CacheResourceCacheEntry(CacheEntry cacheEntry) {
        this.cacheEntry = cacheEntry;
    }


    /*
    @Override
    public InputStream open()  {
        try {
            return clob.getAsciiStream();
        } catch (SQLException e) {
            return null;
        }
    }*/

    /**
     * This class streams the result set.
     * Take care to close it. It auto-closes on consumption.
     *
     * @return
     */
    @Override
    public ResultSet asResultSet() {
        try {
            return _asResultSet();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public ResultSet _asResultSet()
            throws SQLException
    {
        final InputStream in = cacheEntry.getInputStream();
        ResultSet resultSet = ResultSetFactory.fromXML(in);
        //IClosable closable = new ClosableCacheSql(this, in);
        IClosable closable = new IClosable() {
            @Override
            public void close() {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Error", e);
                }
            }    
        };
        
        ResultSetClosable result = new ResultSetClosable(resultSet, closable);
        return result;
    }

    @Override
    public boolean isOutdated() {
        return System.currentTimeMillis() - cacheEntry.getTimestamp() > cacheEntry.getLifespan();
    }

    @Override
    public Model asModel(Model result) {
        try {
            return _asModel(result);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Model _asModel(Model result) throws SQLException {
        InputStream in = cacheEntry.getInputStream();

        /*
        ByteArrayInputStream tmp;
        try {
            String str = StreamUtils.toStringSafe(in);
            System.out.println("Content: ");
            System.out.println(str);
            tmp = new ByteArrayInputStream(str.getBytes());

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        in = tmp;
        */

	    result.read(in, null, "N-TRIPLES");
	    close();
//        try {
//            in.close();
//        } catch (Exception e) {
//            logger.warn("Error", e);
//        }
        //cacheEntry.getInputStream().close();
        //close();
        
        return result;
    }
    
    @Override
    public boolean asBoolean() {
        try {
            return _asBoolean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean _asBoolean() throws SQLException, IOException {
        // NOTE: This function closes the stream
        String str = StreamUtils.toString(cacheEntry.getInputStream());

        boolean result = Boolean.parseBoolean(str);

        //cacheEntry.getInputStream().close();
        //close();

        return result;
    }

    @Override
    public void close() {
        try {
            cacheEntry.getInputStream().close();
        } catch (IOException e) {
            //throw new RuntimeException(e);
            logger.warn("Error", e);
        }
    }

}
