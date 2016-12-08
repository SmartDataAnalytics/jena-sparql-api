package org.aksw.jena_sparql_api.cache.extra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 5:12 PM
 */
public class CacheImpl
    implements Cache
{
    private CacheCore cacheCore;

    public CacheImpl(CacheCore cacheCore) {
        this.cacheCore = cacheCore;
    }


    @Override
    public void write(String queryString, ResultSet resultSet) {
        try {
            _write(queryString, resultSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void _write(String queryString, final ResultSet resultSet) throws IOException {
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        new Thread(
          new Runnable(){
            public void run(){
                ResultSetFormatter.outputAsXML(out, resultSet);
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
          }
        ).start();
        cacheCore.write(queryString, in);
    }

    @Override
    public void write(Query query, ResultSet resultSet) {
        write(query.toString(), resultSet);
    }

    @Override
    public void write(String queryString, final Model model) {
        try {
            _write(queryString, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void _write(String queryString, final Model model) throws IOException {
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        new Thread(
          new Runnable(){
            public void run(){
                model.write(out, "N-TRIPLES");
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
          }
        ).start();
        cacheCore.write(queryString, in);
    }

    @Override
    public void write(Query query, Model model) {
        write(query.toString(), model);
    }

    @Override
    public CacheResource lookup(String queryString) {
        CacheEntry cacheEntry = cacheCore.lookup(queryString);
        return cacheEntry == null
                ? null
                : new CacheResourceCacheEntry(cacheEntry);
    }

    @Override
    public CacheResource lookup(Query query) {
        CacheEntry cacheEntry = cacheCore.lookup(query.toString());
        return cacheEntry == null
                ? null
                : new CacheResourceCacheEntry(cacheEntry);
    }


	@Override
	public void write(String queryString, boolean value) {
		try {
            _write(queryString, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
	}
	
	public void _write(String queryString, final boolean value) throws IOException {
        cacheCore.write(queryString, new ByteArrayInputStream(String.valueOf(value).getBytes()));
    }


	@Override
	public void write(Query query, boolean value) {
		write(query.toString(), value);
	}
}
