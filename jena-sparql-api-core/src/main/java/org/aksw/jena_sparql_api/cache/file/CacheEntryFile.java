package org.aksw.jena_sparql_api.cache.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;

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
            	result = new MetaBZip2CompressorInputStream(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
