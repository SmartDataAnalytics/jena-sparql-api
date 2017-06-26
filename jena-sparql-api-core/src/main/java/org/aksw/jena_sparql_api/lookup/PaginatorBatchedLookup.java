package org.aksw.jena_sparql_api.lookup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;

import com.google.common.collect.Range;

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
    public Stream<O> apply(Range<Long> range) {
        Stream<I> baseInStream = base.apply(range);

        Stream<O> result = StreamUtils.mapToBatch(baseInStream, batchSize)
            .flatMap(batch -> {
                Map<I, O> map = lookup.apply(batch);
                Collection<O> values = map.values();

                return values.stream();
            });

        return result;
    }

    @Override
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {
        CountInfo result = base.fetchCount(itemLimit, rowLimit);
        return result;
    }
}
