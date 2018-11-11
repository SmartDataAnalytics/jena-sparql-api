package org.aksw.jena_sparql_api.sparql_path.core;

import java.io.IOException;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.impl.bidirectional.ConceptPathFinderSystemBidirectional;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConceptPathFinder {
	
	private static final Logger logger = LoggerFactory.getLogger(TestConceptPathFinder.class);

	
	@Test
	public void testConceptPathFinder() throws IOException, ParseException {

		Dataset ds = RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl");
		RDFConnection dataConnection = RDFConnectionFactory.connect(ds);
		
		ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
		Model dataSummary = system.computeDataSummary(dataConnection).blockingGet();
		
		ConceptPathFinder pathFinder = system.newFactory()
			.setDataSummary(dataSummary)
			.setDataConnection(dataConnection)
			.create();
				
		
		//Graph<RDFNode, Statement> g = new PseudoGraphJenaModel(model);		
		//Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),
		
		List<Path> paths = pathFinder
				.createSearch(
					Concept.parse("?s | ?s eg:cd ?o", PrefixMapping.Extended),
					Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended)
				)
				.setMaxLength(10)
				.setMaxResults(10)
				.exec()
				.toList().blockingGet();
		
		System.out.println("Paths");
		paths.forEach(System.out::println);
	}
}
