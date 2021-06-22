package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sys.JenaSystem;


/**
 * Simple SPI implementation that delegates content type probing to
 * RDFLanguages.guessContentType
 *
 * @author raven
 *
 */
public class FileTypeDetectorFromRDFLanguages
    extends FileTypeDetector
{
    static {
        // Ensure that Jena plugins are loaded - otherwise we may miss
        // some extensions such as HDT
        JenaSystem.init();
    }

    @Override
    public String probeContentType(Path path) throws IOException {
        ContentType contentType = RDFLanguages.guessContentType(path.toString());
        String result = contentType == null
                ? null
                : contentType.getContentTypeStr();
        return result;
    }

}
