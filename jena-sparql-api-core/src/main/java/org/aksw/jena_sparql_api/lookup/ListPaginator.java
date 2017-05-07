package org.aksw.jena_sparql_api.lookup;

import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.util.collection.RangedSupplier;

import com.google.common.collect.Range;

public interface ListPaginator<T>
    extends RangedSupplier<Long, T>
{
    CountInfo fetchCount(Long itemLimit, Long rowLimit);

    default List<T> fetchList(Range<Long> range) {
        List<T> result = apply(range).collect(Collectors.toList());
        return result;
    }

    default ListPaginator<List<T>> batch(long chunkSize) {
        return new ListPaginatorBatch<T>(this, chunkSize);
    }
}
