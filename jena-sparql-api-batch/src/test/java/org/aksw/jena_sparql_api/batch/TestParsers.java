package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.concept.parser.SparqlRelationParser;
import org.aksw.jena_sparql_api.concept.parser.SparqlRelationParserImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.junit.Test;

import org.apache.jena.query.Syntax;
import com.vividsolutions.jts.util.Assert;

public class TestParsers {
    @Test
    public void testRelationParser() {
        SparqlRelationParser parser = new SparqlRelationParserImpl(new SparqlElementParserImpl(SparqlQueryParserImpl.create(Syntax.syntaxSPARQL_10)));

        BinaryRelation actual = parser.apply("?s ?o | ?s <http://foo.bar/baz> ?o");

        Assert.equals("?s ?o | ?s  <http://foo.bar/baz>  ?o", actual.toString());
    }
}
