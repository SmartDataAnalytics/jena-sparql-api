package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
import org.junit.Test;

public class TestParsers {
    @Test
    public void testRelationParser() {
        SparqlRelationParser parser = new SparqlRelationParserImpl();

        Relation r = parser.apply("?s ?o | ?s <http://foo.bar/baz> ?o");

        System.out.println(r);
    }
}
