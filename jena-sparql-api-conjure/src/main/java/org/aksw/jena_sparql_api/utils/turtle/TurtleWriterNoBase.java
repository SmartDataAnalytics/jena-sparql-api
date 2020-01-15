package org.aksw.jena_sparql_api.utils.turtle;

import static org.apache.jena.riot.WebContent.contentTypeTurtle;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.writer.TurtleShell;
import org.apache.jena.riot.writer.TurtleWriterBase;
import org.apache.jena.sparql.util.Context;

/**
 * A variant of the turtle writer that omits the base url statement.
 * Useful if the base URL corresponds to a directory
 * 
 * @author raven
 *
 */
public class TurtleWriterNoBase
	extends TurtleWriterBase
{
    @Override
    protected void output(IndentedWriter iOut, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        TurtleWriter$ w = new TurtleWriter$(iOut, prefixMap, baseURI, context) ;
        w.write(graph) ;
    }

    private static class TurtleWriter$ extends TurtleShell {
        public TurtleWriter$(IndentedWriter out, PrefixMap prefixMap, String baseURI, Context context) {
            super(out, prefixMap, baseURI, context) ;
        }

        private void write(Graph graph) {
//            writeBase(baseURI) ;
            writePrefixes(prefixMap) ;
            if ( !prefixMap.isEmpty() && !graph.isEmpty() )
                out.println() ;
            writeGraphTTL(graph) ;
        }
    }

	
	public static void register() {
	    final Lang TURTLE_NO_BASE = LangBuilder.create("ttl-nb", contentTypeTurtle + "+nb")
                //.addAltNames("TTL")
                //.addAltContentTypes(contentTypeTurtleAlt1, contentTypeTurtleAlt2)
                //.addFileExtensions("ttl")
                .build() ;		

		final RDFFormat TURTLE_PRETTY_NO_BASE  = new RDFFormat(Lang.TURTLE, new RDFFormatVariant("pretty-no-base"));
		
		WriterGraphRIOTFactory factory = new WriterGraphRIOTFactory() {
	        @Override
	        public WriterGraphRIOT create(RDFFormat serialization)
	        {
	            if ( Objects.equals(TURTLE_PRETTY_NO_BASE, serialization) ) {
	                return new TurtleWriterNoBase() ;
	        	} else {
	        		return null;
	        	}
	        }
		};
	    
	    RDFLanguages.register(TURTLE_NO_BASE);
		RDFWriterRegistry.register(TURTLE_NO_BASE, TURTLE_PRETTY_NO_BASE);
		RDFWriterRegistry.register(TURTLE_PRETTY_NO_BASE, factory);		
	}
}