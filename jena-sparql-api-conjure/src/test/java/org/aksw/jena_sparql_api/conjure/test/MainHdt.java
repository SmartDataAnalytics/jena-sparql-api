package org.aksw.jena_sparql_api.conjure.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.ref.RefImpl;
import org.aksw.jena_sparql_api.conjure.datapod.impl.RdfDataPodHdtImpl;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.graph.Graph;
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
			Model header = new RdfDataPodHdtImpl(RefImpl.create(hdt, hdt::close, null), true).getModel();
			RDFDataMgr.write(System.out, header, RDFFormat.TURTLE_PRETTY);
		}

//		RDFDataMgr.write(System.out, expected, RDFFormat.NTRIPLES);
//		RDFDataMgr.write(System.out, actual, RDFFormat.NTRIPLES);

		Assert.assertTrue(isIsomorphic);
	}
	
	public static void main(String[] args) throws IOException {
//		String filename = "/home/raven/tmp/swdf-2012-11-28.hdt.gz";
		String filename = "/home/raven/public_html/test.hdt";

//		HDT hdt = HDTManager.mapIndexedHDT(filename, null);
		HDT hdt = HDTManager.loadHDT(filename);

		// Create Jena Model on top of HDT.
		Graph graph = new HDTGraph(hdt);
		//graph = GraphWrapperTransform.wrapWithNtripleParse(graph);
		Model model = ModelFactory.createModelForGraph(graph);

		Graph graphFix = GraphUtils.wrapGraphWithNQuadsFix(graph);
		Model modelFix = ModelFactory.createModelForGraph(graphFix);
		
		int n = 3;
		String queryStr = "SELECT * { ?s ?p ?o } LIMIT 1000000";
		for(int i = 0; i < n; ++i) {
				
			Stopwatch sw1 = Stopwatch.createStarted();				
			try(QueryExecution qe = QueryExecutionFactory.create(queryStr, model)) {
				ResultSetFormatter.consume(qe.execSelect());
				//System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
			System.out.println("Plain HDT: " + sw1.elapsed(TimeUnit.MILLISECONDS));

			Stopwatch sw2 = Stopwatch.createStarted();				
			try(QueryExecution qe = QueryExecutionFactory.create(queryStr, modelFix)) {
				ResultSetFormatter.consume(qe.execSelect());
				//System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
			System.out.println("Fixed HDT: " + sw2.elapsed(TimeUnit.MILLISECONDS));

//				if(i == n - 1) {
//			}
		}
		
		
	}
}
