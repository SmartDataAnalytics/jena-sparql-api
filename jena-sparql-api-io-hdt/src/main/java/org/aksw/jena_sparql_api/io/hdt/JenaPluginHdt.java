package org.aksw.jena_sparql_api.io.hdt;

import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginHdt
    implements JenaSubsystemLifecycle {

    public static final Lang LANG_HDT = LangBuilder.create("hdt", "application/x-hdt")
            .addFileExtensions("hdt")
            .build();

    public static final RDFFormat FORMAT_HDT  = new RDFFormat(LANG_HDT, new RDFFormatVariant("default"));


    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    /**
     * Register the HDT language with readers and writers.
     *
     */
    public static void init() {
        // Check to not overwrite a possibly official HDT integration
         if(RDFLanguages.fileExtToLang("hdt") != null) {
             return;
         }

        ReaderRIOTFactory readerFactory = new ReaderRIOTFactory() {
            @Override
            public ReaderRIOT create(Lang language, ParserProfile profile) {
                if (!LANG_HDT.equals(language))
                    throw new InternalErrorException("Attempt to parse " + language + " as HDT");
                // return new JsonLDReader(language, profile, profile.getErrorHandler());
                ReaderRIOT r = new ReaderRIOT_HDT();
                return r;
            };
        };

        WriterGraphRIOTFactory writerFactory = new WriterGraphRIOTFactory() {
            @Override
            public WriterGraphRIOT create(RDFFormat serialization)
            {
                WriterGraphRIOT result = Objects.equals(FORMAT_HDT, serialization)
                    ? new WriterGraphRIOT_HDT()
                    : null;

                return result;
            }
        };

        RDFLanguages.register(LANG_HDT);
        RDFWriterRegistry.register(FORMAT_HDT, writerFactory);
        RDFWriterRegistry.register(LANG_HDT, FORMAT_HDT);
        RDFParserRegistry.registerLangTriples(LANG_HDT, readerFactory);
    }
}
