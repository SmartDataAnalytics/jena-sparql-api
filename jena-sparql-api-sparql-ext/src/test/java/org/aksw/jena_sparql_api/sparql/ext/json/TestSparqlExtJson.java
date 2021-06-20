package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.builders.ExprBuildException;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TestSparqlExtJson {
    public static final PrefixMapping pm = new PrefixMappingImpl();

    static {
        pm.setNsPrefixes(PrefixMappingImpl.Extended);
        JenaExtensionJson.addPrefixes(pm);
    }

    @Test
    public void testJsonEntries() {
        SparqlQueryParser parser = SparqlQueryParserImpl.create(pm);
        Query q = parser.apply("SELECT ?s { BIND(json:path(\"{'x':{'y': 'z' }}\"^^xsd:json, '$.x') AS ?s) }");
//        Query q = parser.apply("SELECT ?s { BIND(json:path(json:entries(\"{'k':'v'}\"^^xsd:json), '$[*].value') AS ?s) }");
//        Query q = parser.apply("SELECT ?s { BIND(json:js('function(x) { return x.v; }', \"{'k':'v'}\"^^xsd:json) AS ?s) }");
        Model m = ModelFactory.createDefaultModel();
        try(QueryExecution qe = QueryExecutionFactory.create(q, m)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }

    }

    @Test
    public void testJsonObjectCreation() {
        JsonObject expected = new JsonObject();
        expected.addProperty("uri", "urn:test");
        expected.addProperty("binsearch", true);

        NodeValue nv = ExprUtils.eval(ExprUtils.parse("json:object('uri', <urn:test>, 'binsearch', true)", pm));
        JsonElement actual = RDFDatatypeJson.extract(nv);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testJsonArrayCreation() {
        JsonArray expected = new JsonArray();
        expected.add("hi");
        expected.add("urn:test");
        expected.add(true);

        NodeValue nv = ExprUtils.eval(ExprUtils.parse("json:array('hi', <urn:test>, true)", pm));

        JsonElement actual = RDFDatatypeJson.extract(nv);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testJsonConversionBoolean() {
        JsonElement expected = new JsonPrimitive(true);
        JsonElement actual = RDFDatatypeJson.extract(ExprUtils.eval(ExprUtils.parse("json:toJson(true)", pm)));
        Assert.assertEquals(expected, actual);
    }

    /** Attempting to create a json object with an odd argument count must fail */
    @Test(expected = ExprBuildException.class)
    public void testJsonObjectCreationOddArguments() {
        ExprUtils.eval(ExprUtils.parse("json:object('uri', <urn:test>, 'binsearch')", pm));
    }
}
