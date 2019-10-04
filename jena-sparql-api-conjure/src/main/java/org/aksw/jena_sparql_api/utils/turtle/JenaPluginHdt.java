package org.aksw.jena_sparql_api.utils.turtle;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFLanguages;

public class JenaPluginHdt {
	public static final Lang HDT = LangBuilder.create("hdt", "application/x-hdt").addFileExtensions("hdt").build();


	/**
	 * Register the HDT language. Especially the file extension is of use.
	 * Note, that HDT is not registered with concrete riot readers (we could add that), because
	 * we want to use HDT Manager 
	 * 
	 */
	public static void register() {
		//if(RDFLanguages.fileExtToLang("hdt") == null) {
		RDFLanguages.register(HDT);
		//}
		
//
//		ReaderRIOTFactory factory = new ReaderRIOTFactory() {
//			@Override
//			public ReaderRIOT create(Lang language, ParserProfile profile) {
//				if (!HDT.equals(language))
//					throw new InternalErrorException("Attempt to parse " + language + " as HDT");
//				// return new JsonLDReader(language, profile, profile.getErrorHandler());
//				return new ReaderRIOT() {
//					
//					@Override
//					public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
//						HDTManager.loadHDT()
//					}
//					
//					@Override
//					public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
//						HDTManager.loadHDT()
//					}
//				};
//			};
//		};

//		RDFParserRegistry.registerLangTriples(HDT, factory);
//		ModelFactory.createDefaultModel()
//			RDFWriterRegistry.register(TURTLE_NO_BASE, TURTLE_PRETTY_NO_BASE);
//			RDFWriterRegistry.register(TURTLE_PRETTY_NO_BASE, factory);		

	}
//	final RDFFormat TURTLE_PRETTY_NO_BASE  = new RDFFormat(Lang.TURTLE, new RDFFormatVariant("pretty-no-base"));
//	
//	WriterGraphRIOTFactory factory = new WriterGraphRIOTFactory() {
//        @Override
//        public WriterGraphRIOT create(RDFFormat serialization)
//        {
//            if ( Objects.equals(TURTLE_PRETTY_NO_BASE, serialization) ) {
//                return new TurtleWriterNoBase() ;
//        	} else {
//        		return null;
//        	}
//        }
//	};

}
