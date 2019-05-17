package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import org.junit.Test;

public class TestBenchmark {

	@Test
	public void testBenchmark() 
	{
//		String queryStr = "" +
//"SELECT * { BIND(<http://jsa.aksw.org/fn/sys/benchmark>('SELECT * { ?s ?p ?o }') AS ?x) }";
		//ARQ.setFalse(ARQ.constantBNodeLabels);
		//String queryStr = "SELECT * { SERVICE <http://localhost:8890/sparql> { <_:test> ?p ?o } }";
//		String queryStr = "SELECT * {\n" + 
//				"  BIND(\"SELECT * { ?s ?p ?o }\" AS ?queryStr)\n" + 
//				"  ?queryStr <http://jsa.aksw.org/fn/sys/benchmark>(?time ?size)\n" + 
//				"}";
//		String queryStr = "SELECT * { 'SELECT * { SERVICE <http://localhost:8890/sparql> { SELECT * { ?s ?p ?o } LIMIT 10 } }' <http://jsa.aksw.org/fn/sys/execSelect> (?x ?y ?z) }";
//		Model model = ModelFactory.createDefaultModel();
//		try(QueryExecution qe = QueryExecutionFactory.create(queryStr, model)) {
//			ResultSet rs = qe.execSelect();
//			ResultSetFormatter.outputAsCSV(System.out, rs);
//		}
	}
}
