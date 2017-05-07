package org.aksw.jena_sparql_api.lookup;

import java.util.List;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.Range;


/**
 * A wrapper for a paginator which groups items into batches
 * A batch is now a first class citizen, and rertrieval and counts refer to the batches
 *
 * @author raven
 *
 * @param <I>
 */
public class ListPaginatorBatch<I>
    implements ListPaginator<List<I>>
{
    protected ListPaginator<I> base;
    protected long batchSize;

    public ListPaginatorBatch(ListPaginator<I> base, long batchSize) {
        super();
        this.base = base;
        this.batchSize = batchSize;
    }

    @Override
    public Stream<List<I>> apply(Range<Long> range) {
        Range<Long> newRange = RangeUtils.multiplyByPageSize(range, batchSize);
        Stream<I> in = base.apply(newRange);

        Stream<List<I>> result = StreamUtils.mapToBatch(in, batchSize);

        return result;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo countInfo = base.fetchCount(itemLimit, rowLimit);

        long baseCount = countInfo.getCount();
        long n = (baseCount + batchSize - 1) / batchSize;

        CountInfo result = new CountInfo(n, countInfo.isHasMoreItems(), itemLimit);
        return result;
    }

}
