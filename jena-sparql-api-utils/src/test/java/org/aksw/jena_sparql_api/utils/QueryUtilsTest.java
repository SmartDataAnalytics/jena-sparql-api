package org.aksw.jena_sparql_api.utils;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Assert;
import org.junit.Test;

public class QueryUtilsTest {

    @Test
    public void testReducedPrefixes() {
        Query query = QueryFactory.create(String.join("\n",
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX foo: <http://example.org/foo/>",
            "PREFIX bar: <http://example.org/bar/>",
            "PREFIX baz: <http://example.org/baz/>",
            "SELECT * {",
            "  { SELECT * {",
            "    ?s a foo:Foo.",
            "  } }",
            "  ?s rdf:type foo:Bar",
            "}"));

        PrefixMapping pm = org.aksw.jena_sparql_api.utils.QueryUtils.usedPrefixes(query);

        Assert.assertEquals(pm.getNsPrefixMap().keySet(), new HashSet<>(Arrays.asList("rdf", "foo")));
        //Query prefixCleanedQuery = QueryTransformOps.shallowCopy(query);
        //prefixCleanedQuery.setPrefixMapping(pm);
        //System.out.println(prefixCleanedQuery);

    }
}
