package org.aksw.jena_sparql_api.rx.util.collection;

import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.commons.util.delegate.AbstractDelegated;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class RangedSupplierTransform<I extends Comparable<I>, T, U>
    extends AbstractDelegated<RangedSupplier<I, U>>
    implements RangedSupplier<I, T>
{
    protected Function<Flowable<U>, Flowable<T>> transform;

//	public RangedSupplierTransform(RangedSupplier<I, U> delegate, Function<U, T> itemTransform) {
//		this(delegate, (s) -> s.map(itemTransform));
//	}

    public RangedSupplierTransform(RangedSupplier<I, U> delegate, Function<Stream<U>, Stream<T>> streamTransform) {
        super(delegate);
    }

    @Override
    public Flowable<T> apply(Range<I> range) {
        Flowable<U> tmp = delegate.apply(range);
        Flowable<T> result = transform.apply(tmp);
        return result;
    }

}
