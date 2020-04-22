package org.aksw.jena_sparql_api.playground;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.junit.Test;

public class LoticoExamples {

    /*
     * SPARQL Extensions
     */

    @Test
    public void testJsonInline() {
        Model model = ModelFactory.createDefaultModel();

        try(QueryExecution qe = QueryExecutionFactory.create(
                  "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX json: <http://jsa.aksw.org/fn/json/>\n"
                + "SELECT * {\n"
                + "  BIND(json:path('{\"k\": \"v\"}'^^xsd:json, '$.k') AS ?v)\n"
                + "}", model)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }
    }

    @Test
    public void testJsonExtern() throws FileNotFoundException, IOException, ParseException {
        Model model = ModelFactory.createDefaultModel();

        Query query = RDFDataMgrEx.loadQuery("sparql-with-json.rq");

        try(QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }
    }


    /*
     * BLANK NODES
     */


    @Test
    public void testBnodeRaw() {
        Model model = RDFDataMgr.loadModel("bnodes-example.ttl");

        try(QueryExecution qe = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", model)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }
    }

    public static RDFConnection wrapWithVirtualBnodeUris(RDFConnection conn, String profile) {
        //ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(vendorLabel, bnodeLabelFn);

        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");

        Set<String> activeProfiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris xform = ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);


        RDFConnection result = RDFConnectionFactoryEx.wrapWithQueryTransform(conn, xform::rewrite);
        return result;
    }

    @Test
    public void testBnodeSkolemized() {
        Model model = RDFDataMgr.loadModel("bnodes-example.ttl");

        RDFConnection rawConn = RDFConnectionFactory.connect(DatasetFactory.wrap(model));
        RDFConnection conn = wrapWithVirtualBnodeUris(rawConn, "jena");

//        SparqlRx.execSelect(() -> conn.query("SELECT * { ?s ?p ?o }"))
//            .forEach(qs -> System.out.println(qs));

        try(QueryExecution qe = conn.query("SELECT * { ?s ?p ?o }")) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }

        String s = SparqlRx.execSelect(() -> conn.query("SELECT * { ?s ?p ?o FILTER(isBlank(?s))}"))
            .firstElement()
            .map(qs -> qs.get("s").asNode().getURI())
            .blockingGet();

        System.out.println("Picked: " + s);


        try(QueryExecution qe = conn.query("SELECT * { ?s ?p ?o FILTER(?s = <" + s + ">)}")) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }
    }

}
