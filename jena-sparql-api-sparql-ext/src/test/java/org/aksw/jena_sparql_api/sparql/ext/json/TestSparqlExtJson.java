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
import org.junit.Test;

public class TestSparqlExtJson {
    @Test
    public void testJsonEntries() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(PrefixMappingImpl.Extended);
        JenaExtensionJson.addPrefixes(pm);
        SparqlQueryParser parser = SparqlQueryParserImpl.create(pm);
        Query q = parser.apply("SELECT ?s { BIND(json:path(\"{'x':{'y': 'z' }}\"^^xsd:json, '$.x') AS ?s) }");
//        Query q = parser.apply("SELECT ?s { BIND(json:path(json:entries(\"{'k':'v'}\"^^xsd:json), '$[*].value') AS ?s) }");
//        Query q = parser.apply("SELECT ?s { BIND(json:js('function(x) { return x.v; }', \"{'k':'v'}\"^^xsd:json) AS ?s) }");
        Model m = ModelFactory.createDefaultModel();
        try(QueryExecution qe = QueryExecutionFactory.create(q, m)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }

    }
}
