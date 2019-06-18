package org.aksw.jena_sparql_api.sparql.ext.gml;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class TestE_Gml2Wkt {
    private static double TOLERANCE = 0.000001;

    // TODO This test fails on some systems due to rounding errors - make it more robust!
    @Test
    public void testGml2Wkt() throws ParseException {
        String queryStr =
                "PREFIX fn: <http://jsa.aksw.org/fn/gml/> SELECT ?g { BIND(fn:gml2Wkt('<gml:LineString srsName=\"EPSG:25832\"><gml:coordinates>663957.75944074022118,5103981.64908889029175 663955.915655555087142,5103991.151674075052142</gml:coordinates></gml:LineString>') AS ?g) }";
        String[] tmpActual = {null};
        QueryExecutionFactory.create(queryStr, ModelFactory.createDefaultModel()).execSelect()
                .forEachRemaining(
                        qs -> tmpActual[0] = qs.get("g").asNode().getLiteralLexicalForm());
        String actual = tmpActual[0];
        String expected =
                "LINESTRING (11.12011600889509 46.06974332787273, 11.120095457435852 46.06982923685294)";
        GeometryFactory geometryFactory = new GeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);
        LineString expectedGeo = (LineString) reader.read(expected);
        LineString actualGeo = (LineString) reader.read(tmpActual[0]);
        Assert.assertEquals(true, expectedGeo.equalsExact(actualGeo, TOLERANCE));
    }
}
