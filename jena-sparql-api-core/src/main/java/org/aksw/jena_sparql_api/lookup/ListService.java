package org.aksw.jena_sparql_api.lookup;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public interface ListService<C, T> {
    ListPaginator<T> createPaginator(C concept);

    default Flowable<T> streamData(C concept, Range<Long> range) {
        Flowable<T> result = createPaginator(concept).apply(range);
        return result;
    }

}
