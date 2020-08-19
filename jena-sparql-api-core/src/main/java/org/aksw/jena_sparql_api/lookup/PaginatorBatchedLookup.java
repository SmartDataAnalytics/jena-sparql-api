package org.aksw.jena_sparql_api.lookup;

import java.util.Map.Entry;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * This paginator maps each item through a lookup service by batching the lookup requests
 * It does not change the count of items from the base paginator
 *
 * Note: Retains null values
 *
 * @author raven
 *
 * @param <I>
 * @param <O>
 */
public class PaginatorBatchedLookup<I, O>
    implements ListPaginator<O>
{
    protected ListPaginator<I> base;
    protected LookupService<I, O> lookup;
    protected int batchSize;

    @Override
    public Flowable<O> apply(Range<Long> range) {
        Flowable<I> baseInStream = base.apply(range);

        Flowable<O> result = baseInStream.buffer(batchSize).flatMap(batch -> {
            // Map<I, O> map
            Flowable<O> r = lookup.apply(batch).map(Entry::getValue);
            return r;
        });


//        Stream<O> result = StreamUtils.mapToBatch(baseInStream, batchSize)
//            .flatMap(batch -> {
//                Map<I, O> map = lookup.apply(batch);
//                Collection<O> values = map.values();
//
//                return values.stream();
//            });

        return result;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
        Single<Range<Long>> result = base.fetchCount(itemLimit, rowLimit);
        return result;
    }
}
