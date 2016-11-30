package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;


public class OpTransformTests {
//
//    @Parameters(name = "Transformation {index}: {0}")
//    public static Collection<Object[]> data()
//            throws Exception
//    {
//        List<Object[]> params = new ArrayList<>();
//
//
//
//        return params;
//    }
//
//    protected String name;
//    protected String input;
//    protected String expected;
//
//    public OpTransformTests(String name, String input, String expected) {
//        this.name = name;
//        this.input = input;
//        this.expected = expected;
//    }



    @Test
    public void testDistributeJoinUnion() {
        Query expected = QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { { :a :b :c . :x :y :z } UNION { :d :e :f . :x :y :z } }");

        Op op = Algebra.compile(
                QueryFactory.create("PREFIX : <http://ex.org/> SELECT * { { :a :b :c } UNION { :d :e :f } :x :y :z }"));

        op = TransformDistributeJoinOverUnion.transform(op);
        op = Optimize.apply(new TransformMergeBGPs(), op);
        Query actual = OpAsQuery.asQuery(op);
        actual.setPrefix("", "http://ex.org/");

        System.out.println(expected);
        System.out.println(actual);

        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testBGPToFiltersRoundTrip() {
//        Op op = Algebra.toQuadForm(Algebra.compile(
//                QueryFactory.create("SELECT * { ?s a <http://ex.org/Airport> }")));
        Op op = Algebra.toQuadForm(Algebra.compile(
                QueryFactory.create("SELECT ?s { ?s a ?t . Filter(?t = <http://ex.org/Airport>) }")));

        System.out.println("a:" + op);
        op = TransformReplaceConstants.transform(op);

        Transformer.transform(new TransformFilterPlacement(), op);

        System.out.println("b:" + op);
        op = TransformPushFiltersIntoBGP.transform(op);
        System.out.println("c:" + op);

        Query r = OpAsQuery.asQuery(op);
        System.out.println(r);
        //TransformPushFiltersIntoBGP(
    }

    @Test
    public void testFiltersToBgp1() {
        String queryStr = String.join("\n",
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
                "SELECT *",
                "WHERE {",
                "  ?a a skos:Concept .",
                "  ?b a skos:Concept .",
                "  FILTER (?a != ?b)",
                "}");

        Op op = Algebra.toQuadForm(Algebra.compile(
                QueryFactory.create(queryStr)));

        op = TransformPushFiltersIntoBGP.transform(op);
        System.out.println("op:" + op);

        Query r = OpAsQuery.asQuery(op);
        System.out.println(r);
    }

    @Test
    public void testFiltersToBgp2() {
        String queryStr = String.join("\n",
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
                    "SELECT ?concept",
                    "WHERE {",
                    "  ?concept a skos:Concept .",
                    "  OPTIONAL {",
                    "    ?concept skos:broader ?broader .",
                    "  }",
                    "  FILTER (!BOUND(?broader))",
                    "}");

        Op op = Algebra.toQuadForm(Algebra.compile(
                QueryFactory.create(queryStr)));

        op = TransformPushFiltersIntoBGP.transform(op);
        System.out.println("op:" + op);

        Query r = OpAsQuery.asQuery(op);
        System.out.println(r);

    }

}
