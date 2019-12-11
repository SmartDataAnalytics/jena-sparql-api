package org.aksw.jena_sparql_api.conjure.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.aksw.jena_sparql_api.conjure.datapod.impl.RdfDataPodHdtImpl;
import org.aksw.jena_sparql_api.conjure.datapod.impl.ReferenceImpl;
import org.aksw.jena_sparql_api.utils.hdt.JenaPluginHdt;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;


public class MainHdt {

	@Test
	public void testHdtWriter() throws IOException {
		//Path file = Files.createTempFile("data-", ".hdt");		
		Model expected = ModelFactory.createDefaultModel();
		expected.add(RDF.type, RDF.type, RDF.Property);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFDataMgr.write(out, expected, JenaPluginHdt.FORMAT_HDT);
		out.flush();

		InputStream in = new ByteArrayInputStream(out.toByteArray());
		
		Model actual = ModelFactory.createDefaultModel();
		RDFDataMgr.read(actual, in, JenaPluginHdt.LANG_HDT);		
		boolean isIsomorphic = expected.isIsomorphicWith(actual);
		

		if(false) {
			in = new ByteArrayInputStream(out.toByteArray());
			// in = new BufferedInputStream(Files.newInputStream(Paths.get("/home/raven/.dcat/repository/downloads/localhost/5000/data/lodlaundromat/95/95388162d1fcf14963c670b560a40028/95388162d1fcf14963c670b560a40028.hdt/_content/data.hdt")));
			HDT hdt = HDTManager.loadHDT(in);
			Model header = new RdfDataPodHdtImpl(ReferenceImpl.create(hdt, null, null), true).getModel();
			RDFDataMgr.write(System.out, header, RDFFormat.TURTLE_PRETTY);
		}

//		RDFDataMgr.write(System.out, expected, RDFFormat.NTRIPLES);
//		RDFDataMgr.write(System.out, actual, RDFFormat.NTRIPLES);

		Assert.assertTrue(isIsomorphic);
	}
	
	public static void main(String[] args) throws IOException {
//		String filename = "/home/raven/tmp/swdf-2012-11-28.hdt.gz";
		String filename = "/home/raven/tmp/test.hdt";

//		HDT hdt = HDTManager.mapIndexedHDT(filename, null);
		HDT hdt = HDTManager.loadHDT(filename);

		// Create Jena Model on top of HDT.
		HDTGraph graph = new HDTGraph(hdt);
		Model model = ModelFactory.createModelForGraph(graph);
		
		try(QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o } LIMIT 10", model)) {
			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
		}
		
	}
}
