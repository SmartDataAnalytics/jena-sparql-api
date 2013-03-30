package org.aksw.jena_sparql_api.cache.extra;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.aksw.jena_sparql_api.core.ResultSetClose;
import org.aksw.commons.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;


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
        InputStream in = cacheEntry.getInputStreamProvider().open();
        return new ResultSetClosable(ResultSetFactory.fromXML(in), new ClosableCacheSql(this, in));
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
        InputStream in = cacheEntry.getInputStreamProvider().open();

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
        try {
            in.close();
        } catch (Exception e) {
            logger.warn("Error", e);
        }
        cacheEntry.getInputStreamProvider().close();

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
        String str = StreamUtils.toString(cacheEntry.getInputStreamProvider().open());

        boolean result = Boolean.parseBoolean(str);

        cacheEntry.getInputStreamProvider().close();

        return result;
    }

    @Override
    public void close() {
        cacheEntry.getInputStreamProvider().close();
    }

}
