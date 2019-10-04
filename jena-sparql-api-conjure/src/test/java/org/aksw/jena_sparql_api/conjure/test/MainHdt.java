package org.aksw.jena_sparql_api.conjure.test;

import java.io.IOException;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class MainHdt {
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
