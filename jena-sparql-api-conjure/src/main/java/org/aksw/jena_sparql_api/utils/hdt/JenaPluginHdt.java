package org.aksw.jena_sparql_api.utils.hdt;

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


public class JenaPluginHdt {
	public static final Lang LANG_HDT = LangBuilder.create("hdt", "application/x-hdt")
			.addFileExtensions("hdt")
			.build();

	public static final RDFFormat FORMAT_HDT  = new RDFFormat(LANG_HDT, new RDFFormatVariant("default"));

	/**
	 * Register the HDT language with readers and writers.
	 * 
	 */
	public static void register() {
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
	    		WriterGraphRIOT tmp = new WriterGraphRIOT_HDT();
	        	if (Objects.equals(FORMAT_HDT, serialization) ) {
	                return tmp ;
	        	} else {
	        		return null;
	        	}
	        }
		};
	
		RDFLanguages.register(LANG_HDT);
		RDFWriterRegistry.register(FORMAT_HDT, writerFactory);
		RDFParserRegistry.registerLangTriples(LANG_HDT, readerFactory);
	}
}
