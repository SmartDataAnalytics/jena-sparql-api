package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
import org.junit.Test;

import com.hp.hpl.jena.query.Syntax;
import com.vividsolutions.jts.util.Assert;

public class TestParsers {
    @Test
    public void testRelationParser() {
        SparqlRelationParser parser = new SparqlRelationParserImpl(new SparqlElementParserImpl(SparqlQueryParserImpl.create(Syntax.syntaxSPARQL_10)));

        Relation actual = parser.apply("?s ?o | ?s <http://foo.bar/baz> ?o");

        Assert.equals("?s ?o | ?s  <http://foo.bar/baz>  ?o .", actual.toString());
    }
}
