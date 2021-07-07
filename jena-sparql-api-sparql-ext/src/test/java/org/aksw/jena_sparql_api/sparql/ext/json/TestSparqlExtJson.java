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
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.builders.ExprBuildException;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class TestSparqlExtJson {
    protected static final PrefixMapping pm = new PrefixMappingImpl();
    protected static final SparqlQueryParser parser = SparqlQueryParserImpl.create(pm);

    static {
        pm.setNsPrefixes(PrefixMappingImpl.Extended);
        JenaExtensionJson.addPrefixes(pm);
    }

    @Test
    public void testJsonEntries() {
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
        JsonElement actual = RDFDatatypeJson.extract(ExprUtils.eval(ExprUtils.parse("json:convert(true)", pm)));
        Assert.assertEquals(expected, actual);
    }

    /** Attempting to create a json object with an odd argument count must fail */
    @Test(expected = ExprBuildException.class)
    public void testJsonObjectCreationOddArguments() {
        ExprUtils.eval(ExprUtils.parse("json:object('uri', <urn:test>, 'binsearch')", pm));
    }

    @Test
    public void testJsonArrayLength() {
        NodeValue nv = ExprUtils.eval(ExprUtils.parse("json:length('[1, 2, 3]'^^xsd:json)", pm));
        Assert.assertEquals(3, nv.getInteger().intValue());
    }



    @Test
    public void testconvert() {
        String queryStr = "SELECT ?before ?after {\n"
              + "  { BIND(COALESCE() AS ?before) } UNION\n"
              + "  { BIND(true AS ?before) } UNION\n"
              + "  { BIND(1 AS ?before) } UNION\n"
              + "  { BIND(3.14 AS ?before) } UNION\n"
              + "  { BIND(\"string\" AS ?before) } UNION\n"
              + "  { BIND(<urn:foobar> AS ?before) } UNION\n"
              + "  { BIND('{\"key\": \"value\"}'^^xsd:json AS ?before) } UNION\n"
              + "  { BIND('2021-06-21'^^xsd:date AS ?before) } UNION\n"
              + "  { BIND(str('2021-06-21'^^xsd:date) AS ?before) }"
              + "  BIND(json:convert(?before) AS ?after)\n"
              + "}";
        System.out.println(queryStr);
        Query q = parser.apply(queryStr);
        Model m = ModelFactory.createDefaultModel();
        try(QueryExecution qe = QueryExecutionFactory.create(q, m)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect(), new Prologue(pm)));
        }
    }

    @Test
    public void testJsonObjectCreation2() {
        Query q = parser.apply("SELECT ?jsonObject {\n"
                + "  BIND(1 AS ?value1)\n"
                + "  BIND('key2' AS ?key2)\n"
                + "  BIND(json:object('key1', ?value1, ?key2, 'value2') AS ?jsonObject)\n"
                + "}");
      Model m = ModelFactory.createDefaultModel();
      try(QueryExecution qe = QueryExecutionFactory.create(q, m)) {
          System.out.println(ResultSetFormatter.asText(qe.execSelect()));
      }
    }

    @Test
    public void testJsonArrayCreation2() {
        Query q = parser.apply("SELECT ?jsonArray {\n"
                + "  BIND(true AS ?item1)\n"
                + "  BIND('item2' AS ?item2)\n"
                + "  BIND(json:array(?item1, ?item2, 3, json:object('item', 4)) AS ?jsonArray)\n"
                + "}\n"
                + "");
      Model m = ModelFactory.createDefaultModel();
      try(QueryExecution qe = QueryExecutionFactory.create(q, m)) {
          System.out.println(ResultSetFormatter.asText(qe.execSelect()));
      }
    }


    /** Accessing non-existent paths should raise the {@link PathNotFoundException} */
    @Test(expected = PathNotFoundException.class)
    public void testJsonPathNull() {
        Gson gson = new Gson();
        Object tmp = gson.fromJson("{\"foo\": \"bar\"}", Object.class);
        JsonPath.read(tmp, "$.baz");
    }
}
