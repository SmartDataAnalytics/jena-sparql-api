package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.Step;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableList;


public class TestConceptPathFinder {
	
	private static final Logger logger = LoggerFactory.getLogger(TestConceptPathFinder.class);

	
	@Test
	public void testConceptPathFinder() throws IOException, ParseException {

		// Load some test data and create a sparql connection to it
		Dataset ds = RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl");
		RDFConnection dataConnection = RDFConnectionFactory.connect(ds);
		
		dataConnection.update("DELETE WHERE { ?s a ?t }");
		
		// Set up a path finding system
		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
		
		// Use the system to compute a data summary
		// Note, that the summary could be loaded from any place, such as a file used for caching
		Model dataSummary = system.computeDataSummary(dataConnection).blockingGet();
		
		// Build a path finder; for this, first obtain a factory from the system
		// set its attributes and eventually build the path finder.
		ConceptPathFinder pathFinder = system.newPathFinderBuilder()
			.setDataSummary(dataSummary)
			.setDataConnection(dataConnection)
			.build();
				
		
		//Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),
		
		// Create search for paths between two given sparql concepts
		PathSearch<Path> pathSearch = pathFinder.createSearch(
			Concept.parse("?s | ?s eg:cd ?o", PrefixMapping.Extended),
			Concept.parse("?s | ?s eg:ab ?o", PrefixMapping.Extended));
			//Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended));
		
		// Set parameters on the search, such as max path length and the max number of results
		// Invocation of .exec() executes the search and yields the flow of results
		List<Path> actual = pathSearch
				.setMaxLength(7)
				//.setMaxResults(100)
				.exec()
				.toList().blockingGet();

		System.out.println("Paths");
		actual.forEach(System.out::println);

		// TODO Simply specification of reference paths such as by adding a Path.parse method
		List<Path> expected = Arrays.asList(new Path(ImmutableList.<Step>builder()
				.add(new Step("http://www.example.org/bc", true))
				.add(new Step("http://www.example.org/ab", true))
				.build()));

		Assert.assertEquals(expected, actual);
	}
}
