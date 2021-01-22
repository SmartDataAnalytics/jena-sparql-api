package org.aksw.jena_sparql_api.cache.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class CacheEntryFile
    implements CacheEntry
{
	protected Path file;
    protected long lifespan;
    protected boolean isCompressed;

    public CacheEntryFile(Path file, long lifespan, boolean isCompressed) {
        super();
        this.file = file;
        this.lifespan = lifespan;
        this.isCompressed = isCompressed;
    }

    @Override
    public long getTimestamp() {
        long result;
		try {
			result = Files.getLastModifiedTime(file).toMillis();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
            result = Files.newInputStream(file);//new FileInputStream(file);
            if(isCompressed) {
            	result = new BZip2CompressorInputStream(result, true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
