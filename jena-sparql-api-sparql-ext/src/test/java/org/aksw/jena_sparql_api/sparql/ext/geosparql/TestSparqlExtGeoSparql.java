package org.aksw.jena_sparql_api.sparql.ext.geosparql;

//public class TestSparqlExtGeoSparql {
//
//	@Test
//	public void testNearestPoints() {
//		String queryStr = "PREFIX fn: <http://www.opengis.net/ont/geosparql#> SELECT ?g { BIND(fn:nearestPoints('POINT (0 0)', 'POINT (1 1)') AS ?g) }";
//		String[] tmpActual = {null}; 
//		QueryExecutionFactory.create(queryStr, ModelFactory.createDefaultModel()).execSelect().forEachRemaining(qs -> tmpActual[0] = qs.get("g").asNode().getLiteralLexicalForm());
//
//		String actual = tmpActual[0];
//		// TODO Compare the geometry objects 
//		String expected = "LINESTRING (0 0, 1 1)";
//		Assert.assertEquals(expected, actual);
//	}
//}
