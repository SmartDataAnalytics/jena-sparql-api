package org.aksw.jena_sparql_api.cache.extra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;

/**
 * The cache frontend accepts SPARQL domain objects (such as ResultSets and
 * iterators of triples), serializes them to an InputStream and sends it to the backend.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 5:12 PM
 */
public class CacheFrontendImpl
    implements CacheFrontend
{
    protected CacheBackend cacheBackend;

    protected RDFFormat rdfFormat;
    protected Lang resultSetLang;

    public CacheFrontendImpl(CacheBackend cacheBackend) {
        this(cacheBackend, RDFFormat.RDF_THRIFT, ResultSetLang.RS_Thrift);
    }

    public CacheFrontendImpl(CacheBackend cacheBackend, RDFFormat rdfFormat, Lang resultSetLang) {
        this.cacheBackend = cacheBackend;
        this.rdfFormat = rdfFormat;
        this.resultSetLang = resultSetLang;
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
        new Thread(() -> {
            try {
                ResultSetMgr.write(out, resultSet, resultSetLang);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
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
          new Runnable() {
            public void run() {
                // model.write(out, "N-TRIPLES");
                RDFDataMgr.write(out, model, rdfFormat);
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

    public void _writeTriples(String service, String queryString, Iterator<Triple> it) throws IOException {
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        StreamRDF streamRdf = StreamRDFWriter.getWriterStream(out, rdfFormat);

        new Thread(() -> {
            try {
                streamRdf.start();
                StreamRDFOps.sendTriplesToStream(it, streamRdf);
                streamRdf.finish();
            } finally {
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
                : new CacheResourceCacheEntry(cacheEntry, rdfFormat.getLang(), resultSetLang);
    }

    @Override
    public CacheResource lookup(String service, Query query) {
        CacheEntry cacheEntry = cacheBackend.lookup(service, query.toString());
        return cacheEntry == null
                ? null
                : new CacheResourceCacheEntry(cacheEntry, rdfFormat.getLang(), resultSetLang);
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


    @Override
    public boolean isReadOnly() {
        boolean result = cacheBackend.isReadOnly();
        return result;
    }


    @Override
    public void writeTriples(String service, String queryString, Iterator<Triple> it) {
        try {
            _writeTriples(service, queryString, it);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void writeTriples(String service, Query query, Iterator<Triple> it) {
        writeTriples(service, query.toString(), it);
    }
}
