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
class MapServiceTransformItem<C, K, I, O>
    implements MapService<C, K, O>
{
    private MapService<C, K, I> listService;
    private Function<I, O> fnTransformItem;

    public MapServiceTransformItem(MapService<C, K, I> listService, Function<I, O> fnTransformItem) {
        this.listService = listService;
        this.fnTransformItem = fnTransformItem;
    }

    @Override
    public MapPaginator<K, O> createPaginator(C concept) {
        MapPaginator<K, I> base = listService.createPaginator(concept);
        MapPaginator<K, O> result = MapPaginatorTransformItem.create(base, fnTransformItem);
        return result;
    }

    public static <C, K, I, O> MapServiceTransformItem<C, K, I, O> create(MapService<C, K, I> listService, Function<I, O> fnTransformItem) {
        MapServiceTransformItem<C, K, I, O> result = new MapServiceTransformItem<C, K, I, O>(listService, fnTransformItem);
        return result;
    }
}
