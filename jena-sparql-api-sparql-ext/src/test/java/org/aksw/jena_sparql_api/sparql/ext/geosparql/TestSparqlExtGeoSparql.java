package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

public class TestSparqlExtGeoSparql {
	
	@Test
	public void testSimplify() {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(GeoSPARQL_URI.getPrefixes());

		NodeValue nv = ExprUtils.eval(ExprUtils.parse("geof:simplifyDp('POINT (0 0)'^^geo:wktLiteral)", pm));
		System.out.println(nv);
	}

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
}
