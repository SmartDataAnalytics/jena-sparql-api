package org.aksw.jena_sparql_api.rx.op;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.aksw.commons.rx.op.OperatorLocalOrder;
import org.junit.Assert;
import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;

public class TestOperatorLocalOrder {
    @Test
    public void test() {
        Map<Long, String> map = new LinkedHashMap<>();
        map.put(1l, "a");
        map.put(2l, "b");
        map.put(3l, "c");
        map.put(4l, "d");
        map.put(5l, "e");

        List<String> expected = new ArrayList<>(map.values());

        Random rand = new Random(0);
        List<Entry<Long, String>> list = new ArrayList<>(map.entrySet());

        Collections.shuffle(list, rand);

        List<String> actual = Flowable.fromIterable(list)
            .lift(OperatorLocalOrder.create(1l, i -> i + 1, (a, b) -> a - b, Entry::getKey))
            .map(Entry::getValue)
            .toList()
            .blockingGet();

        Assert.assertEquals(expected, actual);
    }
}
