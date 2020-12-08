package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.path.PathParser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestConceptPathFinder {

    private static final Logger logger = LoggerFactory.getLogger(TestConceptPathFinder.class);


    @Test
    public void testConceptPathFinder() throws IOException, ParseException {

        // Load some test data and create a sparql connection to it
        Dataset ds = RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl");
        RDFConnection dataConnection = RDFConnectionFactory.connect(ds);

        //dataConnection.update("DELETE WHERE { ?s a ?t }");

        // Set up a path finding system
        ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();

        // Use the system to compute a data summary
        // Note, that the summary could be loaded from any place, such as a file used for caching
        Model dataSummary = system.computeDataSummary(dataConnection).blockingGet();

        RDFDataMgr.write(System.out, dataSummary, RDFFormat.TURTLE_PRETTY);

        // Build a path finder; for this, first obtain a factory from the system
        // set its attributes and eventually build the path finder.
        ConceptPathFinder pathFinder = system.newPathFinderBuilder()
            .setDataSummary(dataSummary)
            .setDataConnection(dataConnection)
            .setShortestPathsOnly(false)
            .build();


        //Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),

        // Create search for paths between two given sparql concepts
        PathSearch<SimplePath> pathSearch = pathFinder.createSearch(
            Concept.parse("?s { ?s eg:cd ?o }", PrefixMapping.Extended),
            Concept.parse("?s { ?s eg:ab ?o }", PrefixMapping.Extended));
            //Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended));

        // Set parameters on the search, such as max path length and the max number of results
        // Invocation of .exec() executes the search and yields the flow of results
        List<SimplePath> actual = pathSearch
                .setMaxPathLength(3)
                //.setMaxResults(100)
                .exec()
                .toList().blockingGet();

//		System.out.println("Paths");
//		actual.forEach(System.out::println);

        // TODO Simply specification of reference paths such as by adding a Path.parse method
        List<SimplePath> expected = Arrays.asList(
                SimplePath.fromPropertyPath(PathParser.parse("^eg:bc/^eg:ab", PrefixMapping.Extended)));

        Assert.assertEquals(expected, actual);
    }
}
