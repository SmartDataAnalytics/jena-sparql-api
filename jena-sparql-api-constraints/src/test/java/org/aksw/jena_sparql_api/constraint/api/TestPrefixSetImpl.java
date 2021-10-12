package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jena_sparql_api.constraint.util.PrefixSet;
import org.aksw.jena_sparql_api.constraint.util.PrefixSetImpl;
import org.junit.Assert;
import org.junit.Test;


public class TestPrefixSetImpl {

    @Test
    public void testIntersection() {
        PrefixSet a = PrefixSetImpl.create("aa", "b");

        // bb should get shortened to b
        a.intersect(PrefixSetImpl.create("a", "bb"));
        Assert.assertEquals(PrefixSetImpl.create("a", "b"), a);


        // now b should be dropped
        a.intersect(PrefixSetImpl.create("a"));
        Assert.assertEquals(PrefixSetImpl.create("a"), a);


    }

}
