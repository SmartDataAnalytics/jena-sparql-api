package org.aksw.jena_sparql_api.mapper.parallel;

import org.aksw.jena_sparql_api.mapper.Accumulator;

/**
 * Aggregator whose accumulator count the number of seen input objects.
 * 
 * @author raven
 *
 * @param <I> The input type
 */
public class AggCounting<I>
	implements ParallelAggregator<I, Long, Accumulator<I, Long>>
{
	@Override
	public Accumulator<I, Long> createAccumulator() {
		return new AccCounting(0);
	}

	@Override
	public Accumulator<I, Long> combine(Accumulator<I, Long> a, Accumulator<I, Long> b) {
		long count1 = a.getValue();
		long count2 = b.getValue();
		long newCount = count1 + count2;
		
		return new AccCounting(newCount);
	}

	
	public class AccCounting
		implements Accumulator<I, Long>
	{
		protected long count = 0;
		
		public AccCounting(long count) {
			super();
			this.count = count;
		}

		@Override
		public void accumulate(I binding) {
			++count;
		}

		@Override
		public Long getValue() {
			return count;
		}
	}
}
