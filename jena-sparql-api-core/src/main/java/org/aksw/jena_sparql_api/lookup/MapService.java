package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.utils.QueryUtils;

import com.google.common.collect.Range;

/**
 * A list service is actually quite similar to JPA's TypedQuery,
 * the main difference is, that we use a Map<K, V> here instead of a
 * List&lt;T&gt;
 * The rationale behind that is to be able to use the keys with further LookupServices
 * I also had in mind retrieving data from JSON documents, where (before json-ld) there was no standard
 * way for a designated ID attribute; so the idea was, that the list service indexes the result by whatever is the key.
 *
 * Actually, I think I did something clever anyway: When we fetch a resources, shape, we map each node to its
 * corresponding graph, so we use exactly this interface
 *
 * So we should separate ListService yielding a (paginator of) Map from one that yields a List.
 *
 */
public interface MapService<C, K, V>
    // extends Function<C, Paginator<K, V>>
{
    MapPaginator<K, V> createPaginator(C concept);


    // Shorthands

    default Map<K, V> fetchData(C concept, Range<Long> range) {
        Map<K, V> result = createPaginator(concept).fetchMap(range);
        return result;
    }

    /**
     * Select Distinct ?v {
     *     { Select ?v {
     *         concept
     *     } Limit rawLimit }
     * } Limit resLimit
     *
     * @param concept
     * @param itemLimit Limit applied on the set of distinct items (resources)
     * //@param rowLimit Limits the number of rows to scan before applying distinct
     * @return
     */
    default CountInfo fetchCount(C concept, Long itemLimit, Long rowLimit) {
        CountInfo result = createPaginator(concept).fetchCount(itemLimit, rowLimit);
        return result;
    }


    // Convenience functions

    /**
     * The recommended map type to be used as the return value is LinkedHashMap.
     *
     * @param concept
     * @param limit
     * @param offset
     * @return
     */
    default Map<K, V> fetchData(C concept, Long limit, Long offset) {
        Range<Long> range = QueryUtils.createRange(limit, offset);
        Map<K, V> result = fetchData(concept, range);
        return result;
    }
}
