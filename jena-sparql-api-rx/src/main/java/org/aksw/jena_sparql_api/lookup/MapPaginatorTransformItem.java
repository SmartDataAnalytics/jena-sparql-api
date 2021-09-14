package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.apache.jena.ext.com.google.common.collect.Maps;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * FIXME Possibly extend with generic transform instead of just value
 *
 * @author raven
 *
 * @param <K>
 * @param <I>
 * @param <O>
 */
public class MapPaginatorTransformItem<K, I, O>
    implements MapPaginator<K, O>
{
    protected MapPaginator<K, I> delegate;
    protected BiFunction<? super K, ? super I, ? extends O> fnTransformItem;

    public MapPaginatorTransformItem(MapPaginator<K, I> delegate, BiFunction<? super K, ? super I, ? extends O> fnTransformItem) {
        this.delegate = delegate;
        this.fnTransformItem = fnTransformItem;
    }

//    @Override
//    public Map<K, O> fetchMap(Range<Long> range) {
//        //Map<K, I> map = delegate.fetchData(range);
//
//        // Create an intermediary list so that in case of any
//        // error, such as duplicate key, we can investigate the problem
//        //List<Entry<K, O>> items = apply(range).collect(Collectors.toList());
//
////        Map<K, O> result = items.stream()//apply(range)
//          Map<K, O> result = apply(range)
//                .collect(Collectors.toMap(
//                        Entry<K, O>::getKey,
//                        Entry<K, O>::getValue,
//                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
//                        LinkedHashMap::new));
//
////        Map<K, O> result = new LinkedHashMap<K, O>();
////        for(Entry<K, I> entry : map.entrySet()) {
////            K k = entry.getKey();
////            I i = entry.getValue();
////            O o = fnTransformItem.apply(i);
////
////            result.put(k, o);
////        }
//
//        return result;
//    }

    @Override
    public Flowable<Entry<K, O>> apply(Range<Long> range) {
        return delegate.apply(range).map(e ->
            Maps.immutableEntry(e.getKey(), fnTransformItem.apply(e.getKey(), e.getValue())));
    }


    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Single<Range<Long>> result = delegate.fetchCount(itemLimit, rowLimit);
        return result;
    }

    public static <K, I, O> MapPaginatorTransformItem<K, I, O> create(MapPaginator<K, I> listService, BiFunction<? super K, ? super I, ? extends O> fnTransformItem) {
        MapPaginatorTransformItem<K, I, O> result = new MapPaginatorTransformItem<K, I, O>(listService, fnTransformItem);
        return result;
    }
}
