package org.aksw.jena_sparql_api.core.utils;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Test;


public class QueryGenerationUtilsTests {
    public static final Var s = Var.alloc("s");
    public static final Var p = Var.alloc("p");


    public static void eval(
            String inputStr,
            Function<? super Query, ? extends Query> transform,
            String expectedStr) {
        Query input = QueryFactory.create(inputStr);
        Query expected = QueryFactory.create(expectedStr);

        Query actual = transform.apply(input);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGroupByToDistinct1() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Query actual = input.cloneQuery();
                QueryGenerationUtils.optimizeGroupByToDistinct(actual, true);
                return actual;
            },
            "SELECT DISTINCT ?s ?p { ?s ?p ?o }"
        );
    }

    @Test
    public void testGroupByToDistinct2() {
        eval(
            "SELECT ?s (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Query actual = QueryGenerationUtils.project(input, Arrays.asList(s));
                return actual;
            },
            "SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p"
        );
    }

    @Test
    public void testProject0() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                List<Var> vars = Arrays.asList(s);
                Query actual = QueryGenerationUtils.project(input, true, vars);
                return actual;
            },
            "SELECT DISTINCT ?s { SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p }"
        );
    }


    @Test
    public void testProject1() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                List<Var> vars = Arrays.asList(s, p, Var.alloc("c"));
                Query actual = QueryGenerationUtils.project(input, true, vars);
                return actual;
            },
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p"
        );
    }

    @Test
    public void testProject2() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                List<Var> vars = Arrays.asList(s, p);
                Query actual = QueryGenerationUtils.project(input, true, vars);
                return actual;
            },
            "SELECT DISTINCT ?s ?p { ?s ?p ?o }"
        );
    }


    @Test
    public void testOptimizeAggToDistinctX() {
        Query query = QueryFactory.create("SELECT (?s AS ?x) ?o { ?s ?p ?o } GROUP BY ?s ?o");
//        QueryGenerationUtils.optimizeAggregationToDistinct(query);
        System.out.println("TODO Validate testOptimizeAggToDistinctX");
        System.out.println(QueryGenerationUtils.analyzeDistinctVarSets(query));
        System.out.println(query);
    }

    @Test
    public void testOptimizeAggToDistinct0() {
        eval(
            "SELECT ?s { ?s ?p ?o }",
            input -> {
                Query actual = QueryGenerationUtils.distinct(input);
                return actual;
            },
            "SELECT DISTINCT ?s { ?s ?p ?o }"
        );
    }

    @Test
    public void testOptimizeAggToDistinct1() {
        eval(
            "SELECT (?s AS ?x) ?o { ?s ?p ?o } GROUP BY ?s ?o",
            input -> {
                Query actual = QueryGenerationUtils.distinct(input);
                return actual;
            },
            "SELECT DISTINCT (?s AS ?x) ?o { ?s ?p ?o }"
        );
    }


    @Test
    public void testOptimizeAggToDistinct2() {
        eval(
            "SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Query actual = QueryGenerationUtils.distinct(input);
                return actual;
            },
            "SELECT DISTINCT ?s { SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p }"
        );
    }

    @Test
    public void testOptimizeAggToDistinct3() {
        eval(
            "SELECT ?s ?p { ?s ?p ?o } GROUP BY ?s ?p ?o",
            input -> {
                Query actual = QueryGenerationUtils.distinct(input);
                return actual;
            },
            "SELECT DISTINCT ?s ?p { SELECT ?s ?p { ?s ?p ?o } GROUP BY ?s ?p ?o }"
        );
    }

    @Test
    public void testCountQueryGenerationA1() {
        eval(
            "SELECT ?s ?o { ?s ?p ?o }",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, null, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { ?s ?p ?o }"
        );
    }

    @Test
    public void testCountQueryGenerationA2() {
        eval(
           "SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Entry<Var, Query> actual = QueryGenerationUtils.createQueryCountCore(input, null, null);
                return actual.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p }"
        );
    }

    @Test
    public void testCountQueryGenerationA3() {
        eval(
            "SELECT ?s (AVG(?o) AS ?c) { ?s ?p ?o } GROUP BY ?s",
             input -> {
//                 Collection<Var> vars = Arrays.asList(s, Var.alloc("c"));
                 Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, null, null);
                 return count.getValue();
             },
             "SELECT (COUNT(DISTINCT ?s) AS ?c_1) { ?s ?p ?o }"
         );
    }

    @Test
    public void testCountQueryGenerationA4() {
        eval(
            "SELECT ?s (AVG(?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, null, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT ?s { ?s ?p ?o } GROUP BY ?s ?p }"
        );
    }


    @Test
    public void testCountQueryGeneration2() {
        eval(
            "SELECT (?s AS ?x) (COUNT(DISTINCT ?p) AS ?y) { ?s ?p ?o } GROUP BY ?s LIMIT 10",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 7l, 3l);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT DISTINCT (?s AS ?x) { SELECT * { ?s ?p ?o } LIMIT 3 } LIMIT 7 }"
        );
    }

    @Test
    public void testCountQueryGenerationFacete() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Query actual = QueryGenerationUtils.distinct(input);
                return actual;
            },
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p"
        );
    }

    @Test
    public void testCountQueryGenerationFaceteCount() {
        eval(
            "SELECT ?s ?p (COUNT(DISTINCT ?o) AS ?c) { ?s ?p ?o } GROUP BY ?s ?p",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, null, null);
                return count.getValue();
            },
            // "SELECT (COUNT(*) AS ?c_1) { SELECT DISTINCT ?s ?p { ?s ?p ?o } }"
            "SELECT (COUNT(*) AS ?c_1) { SELECT DISTINCT ?s ?p { ?s ?p ?o } }"
        );
    }


    @Test
    public void testCountQueryGenerationWithLimit() {
        eval(
            "SELECT * { ?s ?p ?o } LIMIT 10 OFFSET 3",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 5l, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT * { ?s ?p ?o } LIMIT 5 OFFSET 3 }"
        );
    }

    /**
     * The limit for distinct result rows should be ignored because the query does not use distinct
     *
     */
    @Test
    public void testCountQueryGenerationWithLimit2() {
        eval(
            "SELECT * { ?s ?p ?o } LIMIT 10 OFFSET 3",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 5l, 4l);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT * { ?s ?p ?o } LIMIT 5 OFFSET 3 }"
        );
    }

    /**
     * The query uses distinct so the distinctLimit (=4) must be included in the rewritten query
     *
     */
    @Test
    public void testCountQueryGenerationWithLimit3() {
        eval(
            "SELECT DISTINCT * { ?s ?p ?o } LIMIT 10 OFFSET 3",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 5l, 4l);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT DISTINCT * { SELECT * { ?s ?p ?o } LIMIT 4 } OFFSET 3 LIMIT 5 }"
        );
    }

    /**
     * The query uses distinct so the distinctLimit (=4) must be included in the rewritten query
     *
     */
    @Test
    public void testCountQueryGenerationForLsqBug() {
        eval(
            "SELECT * { ?s a ?o . ?o ?p ?o2 } LIMIT 100",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 1000l, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT * { ?s a ?o . ?o ?p ?o2  } LIMIT 100 }"
        );
    }

    @Test
    public void testCountQueryGenerationForLimitBug() {
        eval(
            "SELECT * { ?s ?p ?o } LIMIT 100 OFFSET 5",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 50l, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT * { ?s ?p ?o } LIMIT 50 OFFSET 5 }"
        );
    }

    /**
     * Queries only with constants caused an exception in the rewrite stating that there need to be
     * variables
     *
     */
    @Test
    public void testCountQueryGenerationForLsqBug2() {
        eval(
            "SELECT * { <urn:s> <urn:p> <urn:o> }",
            input -> {
                Entry<Var, Query> count = QueryGenerationUtils.createQueryCountCore(input, 1000l, null);
                return count.getValue();
            },
            "SELECT (COUNT(*) AS ?c_1) { SELECT * { <urn:s> <urn:p> <urn:o> } LIMIT 1000 }"
        );
    }

}
