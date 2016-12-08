package org.aksw.jena_sparql_api.cache.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.utils.model.Quadlet;
import org.aksw.jena_sparql_api.views.index.CandidateViewSelectorImpl;
import org.aksw.jena_sparql_api.views.index.QuadPrefixes;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestCandidateViewSelector {

    @Test
    public void testCandidateViewSelector() {
        CandidateViewSelectorImpl<String> cvs = new CandidateViewSelectorImpl<>();

        cvs.put(QuadPrefixes.ALWAYS_MATCHING, "hello");

        QuadPrefixes qp1 = new QuadPrefixes(new Quadlet<NavigableSet<String>>(
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://dbpedia.org/resource/")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://www.w3.org/2002/07/owl#sameAs")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://linkedgeodata.org/triplify/"))
                ), true, false);


        cvs.put(qp1, "world");

        {
            Expr expr = ExprUtils.parse("?s = <http://dbpedia.org/resource/Leipzig>");
            Collection<Entry<QuadPrefixes, String>> rs = cvs.apply(expr);

            Assert.assertEquals(rs.size(), 2);
        }

        {
            Expr expr = ExprUtils.parse("?p = <http://dbpedia.org/resource/Leipzig>");
            Collection<Entry<QuadPrefixes, String>> rs = cvs.apply(expr);

            Assert.assertEquals(rs.size(), 1);
        }

    }

    @Test
    public void testPrefixOps()  {
        QuadPrefixes a = new QuadPrefixes(new Quadlet<NavigableSet<String>>(
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("foo")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://dbpedia.org/resource/Leipzig")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://foobar.org/p1")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://linkedgeodata.org/triplify/"))
                ), true, false);

        QuadPrefixes b = new QuadPrefixes(new Quadlet<NavigableSet<String>>(
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("bar")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://dbpedia.org/resource/")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://foobar.org/p2")),
                (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://linkedgeodata.org/triplify/node123"))
                ), true, true);

        QuadPrefixes c = QuadPrefixes.intersect(a, b);
        //System.out.println(c);

        QuadPrefixes d = QuadPrefixes.union(a, b);
        //System.out.println(d);
    }

    @Test
    public void testPredicatePartition() {
        CandidateViewSelectorImpl<String> cvs = new CandidateViewSelectorImpl<>();

        for(int i = 0; i < 100; ++i) {
            QuadPrefixes qp1 = new QuadPrefixes(new Quadlet<NavigableSet<String>>(
                    (NavigableSet<String>)new TreeSet<>(Arrays.asList("")),
                    (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://dbpedia.org/resource/")),
                    (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://foobar.org/pred-" + i)),
                    (NavigableSet<String>)new TreeSet<>(Arrays.asList("http://linkedgeodata.org/triplify/"))
                    ), true, false);

            cvs.put(qp1, "pred" + i);
        }


        {
            // Note: right now the lookup also matches pred-5 - should this behavior be changed, update the assertion
            Expr expr = ExprUtils.parse("?p = <http://foobar.org/pred-50>");
            Collection<Entry<QuadPrefixes, String>> rs = cvs.apply(expr);
            System.out.println("GOT: " + rs);
            Assert.assertEquals(rs.size(), 2);
        }

    }

}
