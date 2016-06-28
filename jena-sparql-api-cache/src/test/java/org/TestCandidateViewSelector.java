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

    @Test
    public void testPredicatePartition() {
        CandidateViewSelectorImpl<String> cws = new CandidateViewSelectorImpl<>();

        for(int i = 0; i < 100; ++i) {
            QuadPrefixes qp1 = new QuadPrefixes(new Quadlet<Collection<String>>(
                    Arrays.asList(""),
                    Arrays.asList("http://dbpedia.org/resource/"),
                    Arrays.asList("http://foobar.org/pred-" + i),
                    Arrays.asList("http://linkedgeodata.org/triplify/")
                    ), true, false);

            cws.put(qp1, "pred" + i);
        }


        {
            // Note: right now the lookup also matches pred-5 - should this behavior be changed, update the assertion
            Expr expr = ExprUtils.parse("?p = <http://foobar.org/pred-50>");
            Collection<String> rs = cws.apply(expr);
            Assert.assertEquals(rs.size(), 2);
        }

    }

}
