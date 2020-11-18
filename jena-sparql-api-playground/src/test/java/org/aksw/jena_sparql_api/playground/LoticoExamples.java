package org.aksw.jena_sparql_api.playground;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.Vars;
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
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.junit.Test;

public class LoticoExamples {

    /*
     * Pimp my query execution
     *
     */
    @Test
    public void testEnhancedQueryExecution() {

        SparqlService ss = FluentSparqlService
            .http("https://databus.dbpedia.org/repo/sparql")
            .config()
                .configQuery()
                    .withDelay(1, TimeUnit.SECONDS)
                    // .withCache(new CacheBackendMem())
                    .withCache(new CacheBackendFile(Paths.get("/tmp/cache"), 600000l, true, false, true))
                    .withPagination(100)
                    .withDefaultLimit(10, true)
                .end()
            .end()
            .create();

        try(RDFConnection baseConn = ss.getRDFConnection()) {
          try(RDFConnection appConn = baseConn) {
                String queryStr = "SELECT * { ?s a <http://dataid.dbpedia.org/ns/core#DataId> ;"
                        + " <http://dataid.dbpedia.org/ns/core#associatedAgent> <https://vehnem.github.io/webid.ttl#this> }";
                SparqlRx.execSelect(appConn, queryStr)
                    .forEach(qs -> System.out.println(qs));
            }
        }

    }

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


    /*
     * Query over Views - Wikidata
     *
     * Given:
     *
     * wd:P400 a wikibase:Property
     *   rdfs:label "platform" ;
     *   wikibase:claim p:400 .
     *
     * p:400 a ObjectProperty .
     *
     * Goal:
     *   p:400 a ObjectProperty ;
     *   rdfs:label "platform" .
     *
     *
     * SELECT * { ?s ?p ?o . FILTER(?x = <http://www.wikidata.org/prop/P400) }
     * SELECT * { ?s <http://wikiba.se/ontology#claim> ?x ; ?p ?o FILTER(?x = <http://www.wikidata.org/prop/P400>) }
     */
    @Test
    public void testQueryOverViews() {
        List<TernaryRelation> views = Arrays.asList(
                new TernaryRelationImpl(Concept.parseElement("{ ?s ?p ?o FILTER(?p = rdf:type && ?o = owl:ObjectProperty) }", PrefixMapping.Extended), Vars.s, Vars.p, Vars.o),
                new TernaryRelationImpl(Concept.parseElement(
                        "{ ?c <http://wikiba.se/ontology#claim> ?p ; ?x ?y }", null), Vars.p, Vars.x, Vars.y)
            );

        String queryStr = "SELECT ?s ?o { ?s a <http://www.w3.org/2002/07/owl#ObjectProperty> ; <http://www.w3.org/2000/01/rdf-schema#label> ?o . FILTER(?s = <http://www.wikidata.org/prop/P400>)}";

        try(RDFConnection rawConn = RDFConnectionFactory.connect("https://query.wikidata.org/sparql")) {
            RDFConnection conn = RDFConnectionFactoryEx.wrapWithQueryTransform(rawConn, query -> {
                Query rewritten = VirtualPartitionedQuery.rewrite(views, query);
                System.out.println(rewritten);
                return rewritten;
            });

            try(QueryExecution qe = conn.query(queryStr)) {
                System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        }
    }
}
