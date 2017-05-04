package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.util.collection.RangedEntrySupplier;

import com.google.common.collect.Range;

/**
 * I think the ListService interface should be changed to:
 * ListService.createPaginator(Concept)
 *
 * TODO: There is an overlap with the RangedSupplier
 *
 * @author raven
 *
 */
public interface Paginator<K, V>
    extends RangedEntrySupplier<Long, K, V>
//    extends BiFunction<Long, Long, Map<K, V>>
{
    Map<K, V> fetchData(Range<Long> range);
    CountInfo fetchCount(Long itemLimit, Long rowLimit);
}
