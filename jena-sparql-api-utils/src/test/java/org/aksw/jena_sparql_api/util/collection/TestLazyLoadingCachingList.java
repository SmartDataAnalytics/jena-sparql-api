package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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


        Stream<String> itA = llcl.apply(Range.closedOpen(0l, 10l));
        Stream<String> itB = llcl.apply(Range.closedOpen(5l, 15l));
        Stream<String> itC = llcl.apply(Range.openClosed(3l, 13l));
        Stream<String> itD = llcl.apply(Range.closedOpen(15l, 20l));
        Stream<String> itE = llcl.apply(Range.closedOpen(15l, 20l));

        itA.forEach(x -> System.out.println("[A] got item: " + x));
        itB.forEach(x -> System.out.println("[B] got item: " + x));
        itC.forEach(x -> System.out.println("[C] got item: " + x));
        itD.forEach(x -> System.out.println("[D] got item: " + x));
        itE.forEach(x -> System.out.println("[E] got item: " + x));
    }

}
