package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class TestBenchmark {

	@Test
	public void testBenchmark() 
	{
		String queryStr = "" +
"SELECT * { BIND(<http://jsa.aksw.org/fn/benchmark>('SELECT * { ?s ?p ?o }') AS ?x) }";
		Model model = ModelFactory.createDefaultModel();
		try(QueryExecution qe = QueryExecutionFactory.create(queryStr, model)) {
			ResultSet rs = qe.execSelect();
			ResultSetFormatter.outputAsCSV(System.out, rs);
		}
	}
}
