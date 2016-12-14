package org.aksw.jena_sparql_api.util.collection;

import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Range;

public class RangedSupplierTransform<I extends Comparable<I>, O, P>
	extends RangedSupplierDelegate<I, O, P>
{
	protected Function<Stream<O>, Stream<P>> transform;

	public RangedSupplierTransform(RangedSupplier<I, O> delegate, Function<Stream<O>, Stream<P>> transform) {
		super(delegate);
	}

	@Override
	public Stream<P> apply(Range<I> range) {
		Stream<O> tmp = delegate.apply(range);
		Stream<P> result = transform.apply(tmp);
		return result;
	}

}
