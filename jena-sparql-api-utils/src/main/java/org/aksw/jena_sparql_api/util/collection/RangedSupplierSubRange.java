package org.aksw.jena_sparql_api.util.collection;

import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.utils.RangeUtils;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class RangedSupplierSubRange<I extends Comparable<I>, O>
	implements RangedSupplier<I, O>
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
	public ClosableIterator<O> apply(Range<I> rawRequestRange) {
		long distance = domain.distance(subRange.lowerEndpoint(), rawRequestRange.lowerEndpoint());

		Range<I> shifted = RangeUtils.shift(rawRequestRange, distance, domain, addition);
		Range<I> effective = shifted.intersection(subRange);

		ClosableIterator<O> result = subRangeSupplier.apply(effective);
		return result;
	}

	public static <O> RangedSupplierSubRange<Long, O> create(RangedSupplier<Long, O> subRangeSupplier, Range<Long> subRange) {
		RangedSupplierSubRange<Long, O> result = new RangedSupplierSubRange<>(
				subRangeSupplier,
				subRange,
				DiscreteDomain.longs(),
				(a, b) -> a + b);
		return result;
	}
}
