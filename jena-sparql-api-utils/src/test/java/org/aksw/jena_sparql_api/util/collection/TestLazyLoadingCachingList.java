package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        
        RangedSupplier<Long, String> tmp = new RangedSupplierList<>(items);
        
        // Add some delay
        RangedSupplier<Long, String> itemSupplier = (range) -> {
            System.out.println("Supplier: Requested range: " + range);
            try {
                TimeUnit.MILLISECONDS.sleep(50l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return tmp.apply(range);
        };
        
        
        RangedSupplier<Long, String> llcl = new RangedSupplierLazyLoadingListCache<String>(
                Executors.newFixedThreadPool(4),
                itemSupplier,
                Range.closedOpen(0l, 17l),
                new RangeCostModel());        
        
        
        ClosableIterator<String> itA = llcl.apply(Range.closedOpen(0l, 10l));
        ClosableIterator<String> itB = llcl.apply(Range.closedOpen(5l, 15l));
        ClosableIterator<String> itC = llcl.apply(Range.openClosed(3l, 13l));
        ClosableIterator<String> itD = llcl.apply(Range.closedOpen(15l, 20l));
        ClosableIterator<String> itE = llcl.apply(Range.closedOpen(15l, 20l));
        
        while(itA.hasNext()) {
            System.out.println("[A] got item: " + itA.next());
        }

        while(itB.hasNext()) {
            System.out.println("[B] got item: " + itB.next());
        }

        while(itC.hasNext()) {
            System.out.println("[C] got item: " + itC.next());
        }

        while(itD.hasNext()) {
            System.out.println("[D] got item: " + itD.next());
        }
        
        while(itE.hasNext()) {
            System.out.println("[E] got item: " + itE.next());
        }
        
        
        
    }

}
