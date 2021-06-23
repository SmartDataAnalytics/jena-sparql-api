package org.aksw.jena_sparql_api.lookup;

import java.util.function.BiFunction;

public class ListServiceMapWrapper<C, K, V, T>
    implements ListService<C, T>
{
    protected MapService<C, K, V> delegate;
    protected BiFunction<K, V, T> fn;

    public ListServiceMapWrapper(MapService<C, K, V> delegate, BiFunction<K, V, T> fn) {
        super();
        this.delegate = delegate;
        this.fn = fn;
    }

    @Override
    public ListPaginator<T> createPaginator(C concept) {
        MapPaginator<K, V> base = delegate.createPaginator(concept);
        ListPaginator<T> result = ListPaginatorMapWrapper.create(base, fn);
        return result;
    }

    public static <C, K, V, T> ListService<C, T> create(MapService<C, K, V> delegate, BiFunction<K, V, T> fn) {
        return new ListServiceMapWrapper<>(delegate, fn);
    }
}
