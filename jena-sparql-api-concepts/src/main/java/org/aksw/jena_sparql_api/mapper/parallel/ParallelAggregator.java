package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.stream.Collector;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.mapper.Aggregators;

/**
 * Aggregator suitable for parallel processing.
 * Extends {@link Aggregator} with a method to combine accumulators.
 * 
 * @author raven
 *
 */
public interface ParallelAggregator<I, O, ACC extends Accumulator<I, O>>
	 extends Aggregator<I, O>
{
	ACC createAccumulator();
	
	/**
	 * Combine accumulators. This method is allowed to mutate any of if its arguments,
	 * hence accumulators should be considered exhausted after combination.
	 * 
	 * @param a First participant of combination
	 * @param b Second participant of combination
	 * @return Combined accumulator
	 */
	ACC combine(ACC a, ACC b);

	
	/**
	 * Combine method that works on Objects. Exact type signatures are typically
	 * too unwieldy to be carried around in user code.
	 * Delegates to {@link #combine(Accumulator, Accumulator)} thus its notes apply.
	 * 
	 * @param x First participant of combination
	 * @param y Second participant of combination
	 * @return Combined accumulator
	 */
	default ACC combineRaw(Object x, Object y) {
		@SuppressWarnings("unchecked")
		ACC a = (ACC)x;
		@SuppressWarnings("unchecked")
		ACC b = (ACC)y;
		ACC result = combine(a, b);
		return result;
	}
	
	
	/**
	 * Convert this aggregator to a Java8 collector.
	 * 
	 * @return
	 */
	default Collector<I, ?, O> asCollector() {
		return ParallelAggregators.createCollector(this);
	}
}
