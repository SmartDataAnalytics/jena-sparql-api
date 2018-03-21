package org.aksw.jena_sparql_api.cache.extra;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:26 PM
 */
public class CacheCoreExCompressor
    implements CacheBackend
{
    private CacheBackend decoratee;

    private final CompressorStreamFactory streamFactory = new CompressorStreamFactory();
    private String compression = CompressorStreamFactory.BZIP2;

    public CacheCoreExCompressor(CacheBackend decoratee) {
        this.decoratee = decoratee;
    }

    public static CacheCoreExCompressor wrap(CacheBackend decoratee) {
        return new CacheCoreExCompressor(decoratee);
    }

    public CacheEntry wrap(CacheEntry raw) {
        CacheEntry result;
        if(raw == null) {
            result = null;
        }
        else {
            InputStream in;
            try {
                in = streamFactory.createCompressorInputStream(compression, raw.getInputStream());
            } catch (CompressorException e) {
                throw new RuntimeException(e);
            }
            result = new CacheEntryBase(raw.getTimestamp(), raw.getLifespan(), in);
        }
//                new InputStreamProviderBZip2(raw.getInputStream(), streamFactory, compression));
        return result;
    }

    @Override
    public CacheEntry lookup(String service, String queryString) {
        CacheEntry raw = decoratee.lookup(service, queryString);

        return wrap(raw);
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
        try {
            _write(service, queryString, in);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // FIXME We are storing data in memory here - will break with huge amounts of data
    public void _write(String service, String queryString, InputStream in)
            throws Exception
    {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();

        OutputStream out = streamFactory.createCompressorOutputStream(compression, tmp);

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out);

        /*
        String line;
        while((line = reader.readLine()) != null) {
            writer.println(line);
        }
        writer.flush();
        writer.close();
        */


        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.flush();
        out.close();



        //InputStream test = streamFactory.createCompressorInputStream(compression, new ByteArrayInputStream(tmp.toByteArray()));
        //System.out.println(StreamUtils.toString(test));


        decoratee.write(service, queryString, new ByteArrayInputStream(tmp.toByteArray()));
    }


    public CacheBackend getDecoratee() {
        return decoratee;
    }

	@Override
	public boolean isReadOnly() {
		return false;
	}

    /*
    public static void main(String[] args)
            throws Exception
    {

        QueryExecutionFactory<?> factory = new QueryExecutionFactoryDereference("LATC QA tool <cstadler@informatik.uni-leipzig.de>");

        // Create a cache using a database called 'cache'
        CacheCoreEx cacheBackend = CacheCoreH2.create("cache", 15000000, true);
        CacheEx cacheFrontend = new CacheExImpl(cacheBackend);

        // The following caching query execution factory associates all cache entries
        // with 'the-internet'
        factory = new QueryExecutionFactoryCacheEx(factory, "http://the-internet.org", cacheFrontend);

        Model result = factory.createQueryExecution("DESCRIBE <http://dbpedia.org/resource/London>").execDescribe();
        result.write(System.out, "N-TRIPLES", null);

    }*/
}
