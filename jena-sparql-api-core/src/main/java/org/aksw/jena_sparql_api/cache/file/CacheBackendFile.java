package org.aksw.jena_sparql_api.cache.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheEntry;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class CacheBackendFile
    implements CacheBackend
{
    private File parentFile;
    private long lifespan;


    public CacheBackendFile(File parentFile, long lifespan) {
        this.parentFile = parentFile;
        this.lifespan = lifespan;

        if(!parentFile.exists()) {
            parentFile.mkdirs();
        }

        boolean canWrite = parentFile.canWrite();
        if(!canWrite) {
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
            BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(fos);

            StreamUtils.copyThenClose(in, out);
//            in.close();
//            out.flush();
//            out.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }


        tmpFile.renameTo(file);
    }

}
