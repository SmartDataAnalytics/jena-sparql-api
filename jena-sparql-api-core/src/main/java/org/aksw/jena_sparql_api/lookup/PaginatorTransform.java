package org.aksw.jena_sparql_api.lookup;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Function;
import com.google.common.collect.Range;

public class PaginatorTransform<K, I, O>
    implements Paginator<K, O>
{
    protected Paginator<K, I> delegate;
    protected Function<I, O> fnTransformItem;

    public PaginatorTransform(Paginator<K, I> delegate, Function<I, O> fnTransformItem) {
        this.delegate = delegate;
        this.fnTransformItem = fnTransformItem;
    }

    @Override
    public Map<K, O> fetchData(Range<Long> range) {
        //Map<K, I> map = delegate.fetchData(range);

        Map<K, O> result = apply(range)
                .collect(Collectors.toMap(
                        Entry<K, O>::getKey,
                        Entry<K, O>::getValue,
                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new));

//        Map<K, O> result = new LinkedHashMap<K, O>();
//        for(Entry<K, I> entry : map.entrySet()) {
//            K k = entry.getKey();
//            I i = entry.getValue();
//            O o = fnTransformItem.apply(i);
//
//            result.put(k, o);
//        }

        return result;
    }

    @Override
    public Stream<Entry<K, O>> apply(Range<Long> range) {
        return delegate.apply(range).map(e ->
            new SimpleEntry<>(e.getKey(), fnTransformItem.apply(e.getValue())));
    }


    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = delegate.fetchCount(itemLimit, rowLimit);
        return result;
    }

    public static <K, I, O> PaginatorTransform<K, I, O> create(Paginator<K, I> listService, Function<I, O> fnTransformItem) {
        PaginatorTransform<K, I, O> result = new PaginatorTransform<K, I, O>(listService, fnTransformItem);
        return result;
    }
}
