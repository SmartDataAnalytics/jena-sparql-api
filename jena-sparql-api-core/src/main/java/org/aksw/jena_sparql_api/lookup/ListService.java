package org.aksw.jena_sparql_api.lookup;

import java.util.stream.Stream;

import com.google.common.collect.Range;

public interface ListService<C, T> {
    ListPaginator<T> createPaginator(C concept);

    default Stream<T> streamData(C concept, Range<Long> range) {
        Stream<T> result = createPaginator(concept).apply(range);
        return result;
    }

}
