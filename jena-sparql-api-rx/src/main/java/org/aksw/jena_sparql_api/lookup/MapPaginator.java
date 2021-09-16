package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.rx.util.collection.RangedEntrySupplier;

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
public interface MapPaginator<K, V>
    extends RangedEntrySupplier<Long, K, V>, ListPaginator<Entry<K, V>>
{
    default Map<K, V> fetchMap() {
        return fetchMap(RangeUtils.rangeStartingWithZero);
    }

    default Map<K, V> fetchMap(Range<Long> range) {
        Map<K, V> result = apply(range)
                .toMap(Entry::getKey, Entry::getValue)
                .blockingGet();
//                .collect(Collectors.toMap(
//                        Entry::getKey,
//                        Entry::getValue,
//                        (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
//                        LinkedHashMap::new));
        return result;
    }
}
