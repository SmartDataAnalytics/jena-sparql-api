package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.util.iterator.ClosableIterator;
import org.junit.Test;

import com.google.common.collect.Range;

public class TestLazyLoadingCachingList {

    @Test
    public void test() {
        //fail("Not yet implemented");
        List<String> items = IntStream
                .range(0, 100)
                .mapToObj(i -> "item-" + i)
                .collect(Collectors.toList());
        
        LazyLoadingCachingList<String> llcl = new LazyLoadingCachingList<String>(
                Executors.newFixedThreadPool(4),
                new StaticListItemSupplier<>(items),
                Range.closedOpen(10l, 50l),
                new RangeCostModel());
        
        ClosableIterator<String> it = llcl.retrieve(Range.closedOpen(0l, 20l));
        while(it.hasNext()) {
            System.out.println("got item: " + it.next());
        }
        
    }

}
