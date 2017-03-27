package org.aksw.jena_sparql_api.util.collection;

import java.util.stream.Stream;

import org.aksw.commons.util.contextual.AbstractDelegated;

import com.google.common.collect.Range;

public abstract class RangedSupplierDelegated<I extends Comparable<I>, P>
	extends AbstractDelegated<RangedSupplier<I, P>>
	implements RangedSupplier<I, P>
{
	public RangedSupplierDelegated(RangedSupplier<I, P> delegate) {
		super(delegate);
	}

	@Override
	public Stream<P> apply(Range<I> t) {
		return delegate.apply(t);
	}
//	@Override
//	public ClosableIterator<O> apply(Range<I> t) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
