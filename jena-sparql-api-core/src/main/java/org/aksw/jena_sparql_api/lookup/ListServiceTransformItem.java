package org.aksw.jena_sparql_api.lookup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;

/**
 * A list service that transforms the input concept to another
 * which gets passed to the underlying list service
 *
 */
class ListServiceTransformItem<C, K, I, O>
    implements ListService<C, K, O>
{
    private ListService<C, K, I> listService;
    private Function<I, O> fnTransformItem;

    public ListServiceTransformItem(ListService<C, K, I> listService, Function<I, O> fnTransformItem) {
        this.listService = listService;
        this.fnTransformItem = fnTransformItem;
    }

    @Override
    public Paginator<K, O> createPaginator(C concept) {
        Paginator<K, I> base = listService.createPaginator(concept);
        Paginator<K, O> result = PaginatorTransform.create(base, fnTransformItem);
        return result;
    }

    public static <C, K, I, O> ListServiceTransformItem<C, K, I, O> create(ListService<C, K, I> listService, Function<I, O> fnTransformItem) {
        ListServiceTransformItem<C, K, I, O> result = new ListServiceTransformItem<C, K, I, O>(listService, fnTransformItem);
        return result;
    }
}
