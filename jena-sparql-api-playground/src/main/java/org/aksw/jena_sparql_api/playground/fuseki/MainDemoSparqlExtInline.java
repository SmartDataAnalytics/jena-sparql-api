package org.aksw.jena_sparql_api.playground.fuseki;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class MainDemoSparqlExtInline {
    public static void main(String[] args) {
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

}
