package org.aksw.jena_sparql_api.lookup;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * A list service that transforms the input concept to another
 * which gets passed to the underlying list service
 *
 */
public class MapServiceTransformItem<C, K, I, O>
    implements MapService<C, K, O>
{
    private MapService<C, K, I> listService;
    private BiFunction<? super K, ? super I, ? extends O> fnTransformItem;

    public MapServiceTransformItem(MapService<C, K, I> listService, BiFunction<? super K, ? super I, ? extends O> fnTransformItem) {
        this.listService = listService;
        this.fnTransformItem = fnTransformItem;
    }

    @Override
    public MapPaginator<K, O> createPaginator(C concept) {
        MapPaginator<K, I> base = listService.createPaginator(concept);
        MapPaginator<K, O> result = MapPaginatorTransformItem.create(base, fnTransformItem);
        return result;
    }

    public static <C, K, I, O> MapServiceTransformItem<C, K, I, O> create(MapService<C, K, I> listService, BiFunction<? super K, ? super I, ? extends O> fnTransformItem) {
        MapServiceTransformItem<C, K, I, O> result = new MapServiceTransformItem<C, K, I, O>(listService, fnTransformItem);
        return result;
    }

    public static <C, K, I, O> MapServiceTransformItem<C, K, I, O> create(MapService<C, K, I> listService, Function<? super I, ? extends O> fnTransformItem) {
        MapServiceTransformItem<C, K, I, O> result = new MapServiceTransformItem<C, K, I, O>(listService, (k, v) -> fnTransformItem.apply(v));
        return result;
    }

}
