package org.aksw.jena_sparql_api.io.hdt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


@Ignore // Broken with jena4 due to removal of the interface "GraphStatisticsHandler" - needs update of HDT for Jena4
public class JenaPluginHdtTest {

	public static boolean isIsomorphic(Model expected, Model actual) {
		boolean result;
		
		String everything = "SELECT ?s ?p ?o { ?s ?p ?o }";

		try (QueryExecution qea = QueryExecutionFactory.create(everything, expected);
			QueryExecution qeb = QueryExecutionFactory.create(everything, actual)) {
			
			ResultSetRewindable rsa = ResultSetFactory.copyResults(qea.execSelect());
			ResultSetRewindable rsb = ResultSetFactory.copyResults(qeb.execSelect());
									
			result = ResultSetCompare.equalsByValue(rsa, rsb);
			
			if (!result) {
				rsa.reset();
				rsb.reset();
				System.err.println("Expected:");
				ResultSetFormatter.out(rsa);
				System.err.println("Actual:");
				ResultSetFormatter.out(rsb);
			}
		}
		
		return result;
	}
	
	
	public static void testTask() {
		Model sourceModel = ModelFactory.createDefaultModel();
		sourceModel.setNsPrefixes(PrefixMapping.Standard);

		Resource s = sourceModel.createResource();
		long value = new Random().nextLong();
		s.addLiteral(RDFS.label, value);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFDataMgr.write(baos, sourceModel, JenaPluginHdt.FORMAT_HDT);
		
		ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
		
		Model targetModel = ModelFactory.createDefaultModel();
		RDFDataMgr.read(targetModel, in, JenaPluginHdt.LANG_HDT);
		
		boolean isIso = isIsomorphic(sourceModel, targetModel);
				
		// System.out.println(Thread.currentThread() + " " + isIso);
		Assert.assertTrue(isIso);		
	}
	
	/** Test whether a write/read round trip with a few triples works as expected */
	@Test
	public void testJenaPluginHdt() {
		testTask();
	}

	/** Test whether multithreaded write/read round trip with a few triples works as expected
	 *  The existence of this test is due to exceptions in sansa/spark when using hdt serialization.
	 *  It is to rule out (or detect) bugs with the hdt library in parallel processing settings */
	@Test
	public void testJenaPluginHdtMultithreaded() {
		IntStream.range(0, 1000)
		.parallel().forEach(x -> testTask());
	}

}
