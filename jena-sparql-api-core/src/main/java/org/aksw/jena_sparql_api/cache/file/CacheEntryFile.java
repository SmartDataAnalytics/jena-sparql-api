package org.aksw.jena_sparql_api.cache.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;

public class CacheEntryFile
    implements CacheEntry
{
    private File file;
    private long lifespan;

    public CacheEntryFile(File file, long lifespan) {
        super();
        this.file = file;
        this.lifespan = lifespan;
    }

    @Override
    public long getTimestamp() {
        long result = file.lastModified();
        return result;
    }

    @Override
    public long getLifespan() {
        return lifespan;
    }

    @Override
    public InputStream getInputStream() {
        InputStream result;
        try {
            InputStream in = new FileInputStream(file);
            result = new MetaBZip2CompressorInputStream(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
