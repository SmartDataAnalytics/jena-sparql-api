package org;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.isomorphism.BlockingCacheIterator;
import org.aksw.isomorphism.Cache;
import org.aksw.isomorphism.CachingIterable;
import org.junit.Test;

public class TestBlockingCacheIterator {
    @Test
    public void test() {
        //List<String> testData = Arrays.asList("john", "doe", "alice", "bob");
        List<String> testData = IntStream.range(0, 1000).mapToObj(i -> "item-" + i).collect(Collectors.toList());
        Cache<List<String>> cache = new Cache<>(new ArrayList<>());

        CachingIterable<String> driver = new CachingIterable<>(testData.iterator(), cache);

        BlockingCacheIterator<String> it = new BlockingCacheIterator<>(cache);

        new Thread(() -> {
            for(String item : driver) {
                System.out.println("Driver: " + item);
            }
        }).start();

        while(it.hasNext()) {
            String item = it.next();
            System.out.println("Client: " + item);
        }

    }

}
