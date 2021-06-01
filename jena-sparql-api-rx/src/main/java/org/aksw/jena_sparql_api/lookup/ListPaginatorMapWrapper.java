package org.aksw.jena_sparql_api.lookup;

import java.util.function.BiFunction;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListPaginatorMapWrapper<K, V, T>
    implements ListPaginator<T>
{
    protected MapPaginator<K, V> delegate;
    protected BiFunction<K, V, T> fn;

    public ListPaginatorMapWrapper(MapPaginator<K, V> delegate, BiFunction<K, V, T> fn) {
        super();
        this.delegate = delegate;
        this.fn = fn;
    }

    @Override
    public Flowable<T> apply(Range<Long> range) {
        return delegate.apply(range).map(e -> fn.apply(e.getKey(), e.getValue()));
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Single<Range<Long>> result = delegate.fetchCount(itemLimit, rowLimit);
        return result;
    }

    public static <K, V, T> ListPaginatorMapWrapper<K, V, T> create(MapPaginator<K, V> delegate, BiFunction<K, V, T> fn) {
        return new ListPaginatorMapWrapper<>(delegate, fn);
    }
}
