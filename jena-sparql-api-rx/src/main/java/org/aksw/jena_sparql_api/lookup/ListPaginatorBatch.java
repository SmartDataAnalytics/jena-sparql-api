package org.aksw.jena_sparql_api.lookup;

import java.util.List;

import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


/**
 * A wrapper for a paginator which groups items into batches
 * A batch is now a first class citizen, and retrievals and counts refer to the batches
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
    public Flowable<List<I>> apply(Range<Long> range) {
        Range<Long> newRange = RangeUtils.multiplyByPageSize(range, batchSize);
        Flowable<I> in = base.apply(newRange);

        Flowable<List<I>> result = in.buffer((int)batchSize);
        //Stream<List<I>> result = StreamUtils.mapToBatch(in, (int)batchSize);

        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        return base.fetchCount(itemLimit, rowLimit).map(countInfo -> {
            long baseCount = countInfo.lowerEndpoint();
            long n = (baseCount + batchSize - 1) / batchSize;

            // TODO We silently assume the range to be a singleton if is has an upperBound
            // TODO Add a method that correctly captures the corner cases to RangeUtils.
            Range<Long> result = countInfo.hasUpperBound() ? Range.singleton(n) : Range.atLeast(n);

            //CountInfo result = new CountInfo(n, countInfo.isHasMoreItems(), itemLimit);
            return result;
        });
    }

}
