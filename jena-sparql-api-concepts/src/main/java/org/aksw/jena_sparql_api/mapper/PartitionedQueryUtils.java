package org.aksw.jena_sparql_api.mapper;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Range;

public class PartitionedQueryUtils {

    /**
     * Returns (limit, offset) pairs
     *
     * @param qef
     * @param query
     * @param requestedGridSize
     * @param pageSize
     * @return
     */
//    public List<Entry<Long, Long>> partition(QueryExecutionFactory qef, Query query, int requestedGridSize, Long pageSize) {
//        long count = QueryExecutionUtils.countQuery(query, qef);
//        partition(count, requestedGridSize, pageSize);
//
//
//        PagingQuery pagingQuery = new PagingQuery(this.pageSize, query);
//        Iterator<Query> itQuery = pagingQuery.createQueryIterator((long)(this.page * this.pageSize), null);
//
//    }


    /**
     * Returns a list of ranges in regard to a total number of items that is
     * requested to be split into gridSize partitions of optional size pageSize.
     *
     * @param numberOfItems
     * @param requestedGridSize
     * @param pageSize
     * @return
     */
    public static List<Range<Long>> partition(long numberOfItems, int requestedGridSize, Long pageSize) {
        List<Range<Long>> result = new ArrayList<>();

        long min = 0;
        long max = numberOfItems;

        // last part is equivalent to (long)Math.ceil(max / pageSize)
        long gridSize = pageSize == null ? requestedGridSize : Math.min(requestedGridSize, (max + pageSize - 1) / pageSize);

        long targetSize = (max - min) / gridSize + 1;

        long start = min;
        long end = start + targetSize - 1;

        // TODO We could compute this in parallel
        while (start <= max) {
            if (end >= max) {
                end = max;
            }
            Range<Long> range = Range.closed(start, end);
            result.add(range);
            start += targetSize;
            end += targetSize;
        }

        return result;
    }
}
