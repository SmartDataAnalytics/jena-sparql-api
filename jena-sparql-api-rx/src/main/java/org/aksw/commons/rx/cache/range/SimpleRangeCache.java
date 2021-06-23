package org.aksw.commons.rx.cache.range;

import java.util.List;

import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplierDelegated;
import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


/**
 * A cache that upon any item or count request caches the full range of data
 * into a (java) List and subsequently serves the request from the cache.
 * This class performs only at most a single request to the delegate's apply method.
 * 
 * @author raven
 */
public class SimpleRangeCache<T>
	extends RangedSupplierDelegated<Long, T>
	implements ListPaginator<T>
{
	protected Single<List<T>> cache;
	
	public SimpleRangeCache(RangedSupplier<Long, T> delegate) {
		super(delegate);

		cache = Single.fromCallable(() -> delegate.apply(Range.atLeast(0l))
				.toList().blockingGet()).cache();
	}

	/**
	 * @param itemLimit This argument is ignored
	 * @param rowLimit This argument is Ignored
	 */
	@Override
	public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
		return cache
				.map(list -> Range.lessThan((long)list.size()));
	}

	@Override
	public Flowable<T> apply(Range<Long> range) {
		// Convert the range to integer
		Range<Integer> r = RangeUtils.transform(range, Ints::checkedCast);

		return cache
				.map(list -> RangeUtils.subList(list, r))
				.toFlowable()
				.flatMap(Flowable::fromIterable);
	}

	public static <V> ListPaginator<V> wrap(ListPaginator<V> backend) {
		return new SimpleRangeCache<>(backend);
	}

}
