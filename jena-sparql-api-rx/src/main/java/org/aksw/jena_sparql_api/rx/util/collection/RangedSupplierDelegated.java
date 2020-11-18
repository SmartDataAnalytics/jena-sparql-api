package org.aksw.jena_sparql_api.rx.util.collection;

import org.aksw.commons.util.contextual.AbstractDelegated;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public abstract class RangedSupplierDelegated<I extends Comparable<I>, P>
    extends AbstractDelegated<RangedSupplier<I, P>>
    implements RangedSupplier<I, P>
{
    public RangedSupplierDelegated(RangedSupplier<I, P> delegate) {
        super(delegate);
    }

    @Override
    public Flowable<P> apply(Range<I> t) {
        return delegate.apply(t);
    }
//	@Override
//	public ClosableIterator<O> apply(Range<I> t) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
