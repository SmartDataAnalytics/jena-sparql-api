package org.aksw.jena_sparql_api.lookup;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


public class ListServiceFromList<C, T>
    implements ListService<C, T>
{
    protected List<T> items;
    protected BiPredicate<C, T> predicate;

    public ListServiceFromList(List<T> items, BiPredicate<C, T> predicate) {
        super();
        this.items = items;
        this.predicate = predicate;
    }


    public static <C, T> ListService<C, T> wrap(List<T> items, BiPredicate<C, T> predicate) {
        return new ListServiceFromList<>(items, predicate);
    }


    @Override
    public ListPaginator<T> createPaginator(C concept) {
        return new ListPaginatorList(concept);
    }


    public class ListPaginatorList
        implements ListPaginator<T>
    {
        protected C concept;

        public ListPaginatorList(C concept) {
            super();
            this.concept = concept;
        }


        protected Stream<T> createFilteredList(C concept) {
            return items.stream()
                .filter(item -> predicate.test(concept, item));
        }


        @Override
        public Flowable<T> apply(Range<Long> t) {
            ContiguousSet<Long> set = ContiguousSet.create(t, DiscreteDomain.longs());

            int fromIndex = Ints.saturatedCast(set.first());
            int toIndex = Ints.saturatedCast(set.last());


            Stream<T> stream = createFilteredList(concept).skip(fromIndex).limit(toIndex);

            //List<T> subList = items.subList(fromIndex, toIndex);

            return Flowable.fromStream(stream);
        }

        @Override
        public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
            long count = createFilteredList(concept).count();
            return Single.just(Range.closedOpen(count, count));
        }

    }
}
