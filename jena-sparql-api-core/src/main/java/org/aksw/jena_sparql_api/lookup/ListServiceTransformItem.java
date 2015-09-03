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
    public Map<K, O> fetchData(C concept, Long limit, Long offset) {

        Map<K, I> map = listService.fetchData(concept, limit, offset);

        Map<K, O> result = new LinkedHashMap<K, O>();
        for(Entry<K, I> entry : map.entrySet()) {
            K k = entry.getKey();
            I i = entry.getValue();
            O o = fnTransformItem.apply(i);

            result.put(k, o);
        }

        return result;
    }

    @Override
    public CountInfo fetchCount(C concept, Long itemLimit, Long rowLimit) {
        CountInfo result = listService.fetchCount(concept, itemLimit, rowLimit);
        return result;
    }

    public static <C, K, I, O> ListServiceTransformItem<C, K, I, O> create(ListService<C, K, I> listService, Function<I, O> fnTransformItem) {
        ListServiceTransformItem<C, K, I, O> result = new ListServiceTransformItem<C, K, I, O>(listService, fnTransformItem);
        return result;
    }
}
