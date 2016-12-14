package org.aksw.jena_sparql_api.util.collection;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.RangeUtils;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class RangedSupplierSubRange<I extends Comparable<I>, O>
	implements RangedSupplier<I, O>, Delegated
{
	protected RangedSupplier<I, O> subRangeSupplier;
	protected Range<I> subRange;
	protected DiscreteDomain<I> domain;
	protected BiFunction<I, Long, I> addition;


	public RangedSupplierSubRange(RangedSupplier<I, O> subRangeSupplier, Range<I> subRange, DiscreteDomain<I> domain,
			BiFunction<I, Long, I> addition) {
		super();
		this.subRangeSupplier = subRangeSupplier;
		this.subRange = subRange;
		this.domain = domain;
		this.addition = addition;
	}

	@Override
	public Stream<O> apply(Range<I> rawRequestRange) {
		Range<I> effectiveRange = RangeUtils.makeAbsolute(subRange, rawRequestRange, domain, addition);

		Stream<O> result = subRangeSupplier.apply(effectiveRange);
		return result;
	}
//
//	@Override
//    public <X> X unwrap(Class<X> clazz, boolean reflexive) {
//    	@SuppressWarnings("unchecked")
//		X result = reflexive && this.getClass().isAssignableFrom(clazz)
//    		? (X)this
//    		: subRangeSupplier.unwrap(clazz, true);
//
//    	return result;
//    }


	public static <O> RangedSupplierSubRange<Long, O> create(RangedSupplier<Long, O> subRangeSupplier, Range<Long> subRange) {
		RangedSupplierSubRange<Long, O> result = new RangedSupplierSubRange<>(
				subRangeSupplier,
				subRange,
				DiscreteDomain.longs(),
				(a, b) -> a + b);
		return result;
	}

	@Override
	public Contextual getDelegate() {
		return subRangeSupplier;
	}

}
