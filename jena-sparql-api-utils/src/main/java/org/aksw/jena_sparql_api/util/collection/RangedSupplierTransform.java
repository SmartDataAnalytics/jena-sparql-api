package org.aksw.jena_sparql_api.util.collection;

import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.util.contextual.AbstractDelegated;

import com.google.common.collect.Range;

public class RangedSupplierTransform<I extends Comparable<I>, T, U>
	extends AbstractDelegated<RangedSupplier<I, U>>
	implements RangedSupplier<I, T>
{
	protected Function<Stream<U>, Stream<T>> transform;

//	public RangedSupplierTransform(RangedSupplier<I, U> delegate, Function<U, T> itemTransform) {
//		this(delegate, (s) -> s.map(itemTransform));
//	}

	public RangedSupplierTransform(RangedSupplier<I, U> delegate, Function<Stream<U>, Stream<T>> streamTransform) {
		super(delegate);
	}

	@Override
	public Stream<T> apply(Range<I> range) {
		Stream<U> tmp = delegate.apply(range);
		Stream<T> result = transform.apply(tmp);
		return result;
	}

}
