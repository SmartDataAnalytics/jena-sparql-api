package org.aksw.jena_sparql_api.mapper.parallel;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;

/**
 * Aggregator suitable for parallel processing.
 * Extends {@link Aggregator} with a method to combine accumulators.
 * 
 * @author raven
 *
 */
public interface ParallelAggregator<I, O, ACC extends Accumulator<I, O>>
	// extends Aggregator<B, V>
{
	ACC createAccumulator();
	
	/**
	 * Combine accumulators. This method is allowed to mutate any of if its arguments,
	 * hence accumulators should be considered exhausted after combination.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	ACC combine(ACC a, ACC b);

	
	default ACC combineRaw(Object x, Object y) {
		@SuppressWarnings("unchecked")
		ACC a = (ACC)x;
		@SuppressWarnings("unchecked")
		ACC b = (ACC)y;
		ACC result = combine(a, b);
		return result;
	}
}
