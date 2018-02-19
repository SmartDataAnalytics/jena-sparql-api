package org.aksw.jena_sparql_api.algebra.transform;

import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
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
        String expectedStr = "SELECT ?s { ?s a <http://ex.org/Airport> . ?x ?y ?z }";
        String inputStr = "SELECT ?s { ?s a ?t . Filter(?t = <http://ex.org/Airport>) ?x ?y ?z }";

        Query expected = QueryFactory.create(expectedStr);

        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(inputStr)));

        System.out.println("a:" + op);
        op = TransformReplaceConstants.transform(op);

        System.out.println("b:" + op);
        op = Transformer.transform(new TransformFilterPlacement(), op);

        System.out.println("c: " + op);
        op = TransformPushFiltersIntoBGP.transform(op);

        System.out.println("d:" + op);
        op = Transformer.transform(new TransformQuadsToTriples(), op);

        System.out.println("e:" + op);
        op = Transformer.transform(new TransformMergeBGPs(), op);

        // Clean up projection
        op = TransformMergeProject.transform(op);
        System.out.println("f:" + op);

        // Bad idea to use filter placement here; may isolate quads with their own filters
        // op = Transformer.transform(new TransformFilterPlacement(), op);

        Query actual = OpAsQuery.asQuery(op);
        System.out.println("f: " + actual);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testFiltersToBgp1() {
        String queryStr = String.join("\n",
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
                "SELECT ?a",
                "WHERE {",
                "  ?a a skos:Concept .",
                "  ?b a skos:Concept .",
                "  FILTER (!(?a = ?b)) ", // TODO normalization substitutes ?a != ?b with !(?a = ?b)
                "  FILTER (?b = skos:Foobar) ",
                "}");

        Query query = QueryFactory.create(queryStr);

        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create(queryStr)));

        op = TransformPushFiltersIntoBGP.transform(op);
        System.out.println("op:" + op);

        Query actual = OpAsQuery.asQuery(op);
        actual.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        //System.out.println(r);

        // TODO: Filter order is non deterministic; use LinkedHashSets
        // For new assert using our query equivalence algo
        boolean isMatch = SparqlQueryContainmentUtils.tryMatchOld(query, actual, VarMapper::createVarMapCandidates);
        Assert.assertEquals(true, isMatch);
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

        Query query = QueryFactory.create(queryStr);
        Op op = Algebra.toQuadForm(Algebra.compile(query));

        op = TransformPushFiltersIntoBGP.transform(op);
        System.out.println("op:" + op);

        Query actual = OpAsQuery.asQuery(op);
        actual.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        boolean isMatch = SparqlQueryContainmentUtils.tryMatchOld(query, actual, VarMapper::createVarMapCandidates);
        Assert.assertEquals(true, isMatch);
    }

}
