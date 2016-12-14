package org.aksw.jena_sparql_api.util.collection;

public abstract class RangedSupplierDelegate<I extends Comparable<I>, O, P>
	implements RangedSupplier<I, P>, Delegated
{
	protected RangedSupplier<I, O> delegate;

	public RangedSupplierDelegate(RangedSupplier<I, O> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Contextual getDelegate() {
		return delegate;
	}

//	@Override
//	public ClosableIterator<O> apply(Range<I> t) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
