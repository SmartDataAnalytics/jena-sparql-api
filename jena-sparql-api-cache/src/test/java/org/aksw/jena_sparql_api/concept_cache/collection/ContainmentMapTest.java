package org.aksw.jena_sparql_api.concept_cache.collection;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Test;

public class ContainmentMapTest {


    @Test
    public void test() {
        ContainmentMap<Integer, String> map = new ContainmentMapImpl<>();

        map.put(Sets.newHashSet(), "hello");
        map.put(Sets.newHashSet(1), "world");
        map.put(Sets.newHashSet(1, 2), "test");
        map.put(Sets.newHashSet(1, 2, 3), "!!!");

        System.out.println(map.getAllEntriesThatAreSubsetsOf(Sets.newHashSet(1, 2, 3)));
        System.out.println(map.getAllEntriesThatAreSubsetsOf(Sets.newHashSet()));
    }
}
