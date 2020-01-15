package org.aksw.jena_sparql_api.utils.turtle;

import static org.apache.jena.riot.WebContent.contentTypeTurtle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.WriterGraphRIOTFactory;
import org.apache.jena.sys.JenaSystem;

public class TurtleNoBaseTest {
	
	
	public static void main(String[] args) {

		
		
		Path p = Paths.get("/home/raven/.dcat/test2/c/c.ttl").normalize().toAbsolutePath();

		Model m = ModelFactory.createDefaultModel();
		String base = p.getParent().normalize().toUri().toString();

		RDFDataMgr.read(m, p.toString(), base, Lang.TURTLE);

		System.out.println("Base: " + base);
	
		// This does not work because the base URL is not part of the model,
		// and RDFDataMgr does not support supplying it to the writer
//		RDFDataMgr.write(System.out, m, TURTLE_NO_BASE);

		// This works
		org.apache.jena.rdf.model.RDFWriter writer = m.getWriter("ttl-nb");
		writer.write(m, System.out, base);
		
		
		System.out.println("done");
		
//		RDFDataMgr.read(m, p.toString(), base, Lang.TURTLE);
		
//		RDFWriter writer = RDFWriter.create().format(TURTLE_PRETTY_NO_BASE).source(m.getGraph()).build();

		//RDFWriterRegistry.
		//RDFWriter writer = new RDFW
		//RDFDataMgr.createGraphWriter(TURTLE_PRETTY_NO_BASE);
		
		//RDFDataMgr.write(System.out, m, TURTLE_PRETTY_NO_BASE);
		//RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
		
		
		//org.apache.jena.riot.RDFWriter.create().
		//new RDFWriterRIOT("foo").
//		WriterGraphRIOT writerGraph = new TurtleWriterWithoutBase(); //m.getWriter("ttl");
//		writer.write(m, System.out, base);
		//m.write(System.out, "ttl", base);
		// TODO Get rid of the @base ...

	}
	
}
