package org.aksw.jena_sparql_api.cache.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class CacheBackendFile
    implements CacheBackend
{
    protected File parentFile;
    protected long lifespan;

    protected boolean useCompression;
    protected boolean isReadonly;

    public CacheBackendFile(File parentFile, long lifespan) {
    	this(parentFile, lifespan, true, false);
    }
    
    public CacheBackendFile(File parentFile, long lifespan, boolean useCompression, boolean isReadonly) {
        this.parentFile = parentFile;
        this.lifespan = lifespan;
        this.useCompression = useCompression;
        this.isReadonly = isReadonly;

        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }

        boolean canWrite = parentFile.canWrite();
        if(!isReadonly && !canWrite) {
            throw new RuntimeException("Cache cannot write to: " + parentFile.getAbsolutePath());
        }
    }

    @Override
    public CacheEntry lookup(String service, String queryString) {
        String fileName = StringUtils.urlEncode(service) + "-" + StringUtils.md5Hash(queryString) + ".dat.bz2";
        File file = new File(parentFile, fileName);
//System.out.println(file.getAbsolutePath());
        CacheEntry result;
        result = file.exists()
            ? new CacheEntryFile(file, lifespan)
            : null;

        return result;
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
    	if(isReadonly) {
    		throw new RuntimeException("Cannot write to readonly cache");
    	}
    	
        String fileName = StringUtils.urlEncode(service) + "-" + StringUtils.md5Hash(queryString) + ".dat.bz2";

        // Rename the file once done with writing
        File file = new File(parentFile, fileName);
        if(file.exists()) {
            file.delete();
        }


        String tmpFileName = fileName + ".tmp";

        File tmpFile = new File(parentFile, tmpFileName);
        try {
            if(!tmpFile.exists()) {
                tmpFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(tmpFile);
            OutputStream out = useCompression ? new BZip2CompressorOutputStream(fos) : fos;

            StreamUtils.copyThenClose(in, out);
//            in.close();
//            out.flush();
//            out.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }


        tmpFile.renameTo(file);
    }

	@Override
	public boolean isReadOnly() {
		return isReadonly;
	}

}
