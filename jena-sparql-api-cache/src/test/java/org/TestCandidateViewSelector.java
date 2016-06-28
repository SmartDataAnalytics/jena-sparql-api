package org;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestCandidateViewSelector {

    @Test
    public void testCandidateViewSelector() {
        CandidateViewSelectorImpl<String> cws = new CandidateViewSelectorImpl<>();

        cws.put(QuadPrefixes.ALWAYS_MATCHING, "hello");

        QuadPrefixes qp1 = new QuadPrefixes(new Quadlet<Collection<String>>(
                Arrays.asList(""),
                Arrays.asList("http://dbpedia.org/resource/"),
                Arrays.asList("http://www.w3.org/2002/07/owl#sameAs"),
                Arrays.asList("http://linkedgeodata.org/triplify/")
                ), true, false);


        cws.put(qp1, "world");

        {
            Expr expr = ExprUtils.parse("?s = <http://dbpedia.org/resource/Leipzig>");
            Collection<String> rs = cws.apply(expr);

            Assert.assertEquals(rs.size(), 2);
        }

        {
            Expr expr = ExprUtils.parse("?p = <http://dbpedia.org/resource/Leipzig>");
            Collection<String> rs = cws.apply(expr);

            Assert.assertEquals(rs.size(), 1);
        }

    }
}
