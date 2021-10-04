package org.aksw.jena_sparql_api.lookup;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MapServiceFromListService<C, T, K, V>
    implements MapService<C, K, V>
{
    protected ListService<C, T> listService;
    protected Function<? super T, ? extends K> itemToKey;
    protected Function<? super T, ? extends V> itemToValue;

    public MapServiceFromListService(
            ListService<C, T> listService,
            Function<? super T, ? extends K> itemToKey,
            Function<? super T, ? extends V> itemToValue) {
        super();
        this.listService = listService;
        this.itemToKey = itemToKey;
        this.itemToValue = itemToValue;
    }


    public class MapPaginatorFromListService
        implements MapPaginator<K, V>
    {
        protected ListPaginator<T> listPaginator;

        public MapPaginatorFromListService(ListPaginator<T> listPaginator) {
            super();
            this.listPaginator = listPaginator;
        }

        @Override
        public Flowable<Entry<K, V>> apply(Range<Long> t) {
            Flowable<Entry<K, V>> result = listPaginator.apply(t)
                .map(item -> {
                    K key = itemToKey.apply(item);
                    V value = itemToValue.apply(item);
                    Entry<K, V> r = new SimpleEntry<>(key, value);
                    return r;
                });

            return result;
        }

        @Override
        public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
            return listPaginator.fetchCount(itemLimit, rowLimit);
        }
    }


    @Override
    public MapPaginator<K, V> createPaginator(C concept) {
        ListPaginator<T> listPaginator = listService.createPaginator(concept);
        return new MapPaginatorFromListService(listPaginator);
    }

    public LookupService<K, V> asLookupService(Function<? super Iterable<? extends K>, C> keysToFilter) {
        LookupService<K, V> result = new LookupServiceFromMapService<>(this, keysToFilter);
        return result;
    }

//    public static <C, K, V> MapServiceFromListService<K, V, K, V> createEmpty() {
//        ListService<C, V> ls = new ListServiceFromList<>(Collections.emptyList(), (k, v) -> true);
//        MapServiceFromListService<C, V, K, V> result = new MapServiceFromListService<>(ls, y -> null, x -> x);
//        return result;
//    }
}