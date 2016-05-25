package org.aksw.jena_sparql_api.concept_cache.collection;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;


public class ContainmentMapTest {
    @Test
    public void test() {
        ContainmentMap<Integer, String> map = new ContainmentMapImpl<>();

        map.put(Sets.newHashSet(), "hello");
        map.put(Sets.newHashSet(1), "world");
        map.put(Sets.newHashSet(1, 2), "test");
        map.put(Sets.newHashSet(1, 2, 3), "!!!");

        //System.out.println(map.getAllEntriesThatAreSubsetsOf(Sets.newHashSet(1, 2, 3)));
        //System.out.println(map.getAllEntriesThatAreSupersetOf(Sets.newHashSet()));
        //map.remove(Sets.newHashSet(1, 2, 3));
        // System.out.println(map.getAllEntriesThatAreSupersetOf(Sets.newHashSet(1, 2)));

        Assert.assertEquals(map.getAllEntriesThatAreSupersetOf(Sets.newHashSet(1, 2, 3)).size(), 1);
        Assert.assertEquals(map.getAllEntriesThatAreSupersetOf(Sets.newHashSet(1, 2)).size(), 2);
        Assert.assertEquals(map.getAllEntriesThatAreSupersetOf(Sets.newHashSet()).size(), 4);
    }
}

