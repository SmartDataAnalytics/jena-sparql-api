package org.aksw.jena_sparql_api.lookup;

import java.util.List;

import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListPaginatorFromList<T>
	implements ListPaginator<T>
{
	protected List<T> backend;

	public ListPaginatorFromList(List<T> backend) {
		super();
		this.backend = backend;
	}

	@Override
	public Flowable<T> apply(Range<Long> t) {
		Range<Integer> r = RangeUtils.apply(t, null, (e, v) -> Ints.checkedCast(e));
		List<T> subList = RangeUtils.subList(backend, r);
		return Flowable.fromIterable(subList);
	}

	@Override
	public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
		return Single.just(Range.lessThan((long)backend.size()));
	}
	
	public static <T> ListPaginator<T> wrap(List<T> list) {
		return new ListPaginatorFromList<>(list);
	}
}
