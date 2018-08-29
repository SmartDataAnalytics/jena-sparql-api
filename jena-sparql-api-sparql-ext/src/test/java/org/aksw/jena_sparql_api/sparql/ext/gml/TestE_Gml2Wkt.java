package org.aksw.jena_sparql_api.sparql.ext.gml;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestE_Gml2Wkt {
  @Test
  public void testGml2Wkt() {
    String queryStr =
        "PREFIX fn: <http://jsa.aksw.org/fn/gml/> SELECT ?g { BIND(fn:gml2Wkt('<gml:LineString><gml:coordinates>6.8,5.3 6.1,5.1</gml:coordinates></gml:LineString>') AS ?g) }";
    String[] tmpActual = {null};
    QueryExecutionFactory.create(queryStr, ModelFactory.createDefaultModel())
        .execSelect()
        .forEachRemaining(qs -> tmpActual[0] = qs.get("g").asNode().getLiteralLexicalForm());
    String actual = tmpActual[0];
    String expected = "LINESTRING (6.8 5.3, 6.1 5.1)";
    Assert.assertEquals(expected, actual);
  }
}
