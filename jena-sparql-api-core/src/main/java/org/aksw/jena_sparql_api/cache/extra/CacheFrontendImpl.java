package org.aksw.jena_sparql_api.cache.extra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 5:12 PM
 */
public class CacheFrontendImpl
    implements CacheFrontend
{
    private CacheBackend cacheBackend;

    public CacheFrontendImpl(CacheBackend cacheBackend) {
        this.cacheBackend = cacheBackend;
    }


    @Override
    public void write(String service, String queryString, ResultSet resultSet) {
        try {
            _write(service, queryString, resultSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void _write(String service, String queryString, final ResultSet resultSet) throws IOException {
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
        cacheBackend.write(service, queryString, in);
    }

    @Override
    public void write(String service, Query query, ResultSet resultSet) {
        write(service, query.toString(), resultSet);
    }

    @Override
    public void write(String service, String queryString, final Model model) {
        try {
            _write(service, queryString, model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void _write(String service, String queryString, final Model model) throws IOException {
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
        cacheBackend.write(service, queryString, in);
    }

    @Override
    public void write(String service, Query query, Model model) {
        write(service, query.toString(), model);
    }

    @Override
    public CacheResource lookup(String service, String queryString) {
        CacheEntry cacheEntry = cacheBackend.lookup(service, queryString);
        return cacheEntry == null
                ? null
                : new CacheResourceCacheEntry(cacheEntry);
    }

    @Override
    public CacheResource lookup(String service, Query query) {
        CacheEntry cacheEntry = cacheBackend.lookup(service, query.toString());
        return cacheEntry == null
                ? null
                : new CacheResourceCacheEntry(cacheEntry);
    }


	@Override
	public void write(String service, String queryString, boolean value) {
		try {
            _write(service, queryString, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
	}
	
	public void _write(String service, String queryString, final boolean value) throws IOException {
        InputStream in = new ByteArrayInputStream(String.valueOf(value).getBytes());
	    cacheBackend.write(service, queryString, in);
    }


	@Override
	public void write(String service, Query query, boolean value) {
		write(service, query.toString(), value);
	}
}
