package org.aksw.jena_sparql_api.cache.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import com.google.common.io.MoreFiles;

//import com.google.common.io.Files;

public class CacheBackendFile
    implements CacheBackend
{
    protected Path parentFile;
    protected long lifespan;

    protected boolean useCompression;
    protected boolean isReadonly;
    
    // whether to write out the query to a file in addition to its result set
    protected boolean writeQuery;

    public CacheBackendFile(File parentFile, long lifespan) {
    	this(parentFile.toPath(), lifespan, true, false, false);
    }
    
    public CacheBackendFile(Path parentFile, long lifespan, boolean useCompression, boolean isReadonly, boolean writeQuery) {
        this.parentFile = parentFile;
        this.lifespan = lifespan;
        this.useCompression = useCompression;
        this.isReadonly = isReadonly;
        this.writeQuery = writeQuery;

        if(!isReadonly && !Files.exists(parentFile)) {
            try {
            	Files.createDirectories(parentFile);
            } catch(Exception e) {
            	throw new RuntimeException(e);
            }
        }

        boolean canWrite = Files.isWritable(parentFile);
        if(!isReadonly && !canWrite) {
            throw new RuntimeException("Cache cannot write to: " + parentFile.toAbsolutePath());
        }
    }

    @Override
    public CacheEntry lookup(String service, String queryString) {
        String baseFileName = StringUtils.urlEncode(service) + "-" + StringUtils.md5Hash(queryString);
        
        String fileName = baseFileName + ".dat" + (useCompression ? ".bz2" : "");

        Path file = parentFile.resolve(fileName);
        
//System.out.println(file.getAbsolutePath());
        CacheEntry result;
        result = Files.exists(file)
            ? new CacheEntryFile(file, lifespan, useCompression)
            : null;

        return result;
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
    	if(isReadonly) {
    		throw new RuntimeException("Cannot write to readonly cache");
    	}
    	
        String baseFileName = StringUtils.urlEncode(service) + "-" + StringUtils.md5Hash(queryString);

        String dataFileName = baseFileName + ".dat" + (useCompression ? ".bz2" : "");
        
        if(writeQuery) {
        	Path queryFile = parentFile.resolve(baseFileName + ".sparql");
        	if(!Files.exists(queryFile)) {
        		try {
					MoreFiles.asCharSink(queryFile, StandardCharsets.UTF_8).write(queryString);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
        	}
        }
        
        // Rename the file once done with writing
        Path file = parentFile.resolve(dataFileName);
        if(Files.exists(file)) {
        	try {
				Files.delete(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        }


        String tmpFileName = dataFileName + ".tmp";

        Path tmpFile = parentFile.resolve(tmpFileName);
        try {
            if(!Files.exists(tmpFile)) {
            	Files.createFile(tmpFile);
            }

            OutputStream fos = Files.newOutputStream(tmpFile);
            OutputStream out = useCompression ? new BZip2CompressorOutputStream(fos) : fos;

            StreamUtils.copyThenClose(in, out);
//            in.close();
//            out.flush();
//            out.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }


        try {
			Files.move(tmpFile, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

	@Override
	public boolean isReadOnly() {
		return isReadonly;
	}

}
