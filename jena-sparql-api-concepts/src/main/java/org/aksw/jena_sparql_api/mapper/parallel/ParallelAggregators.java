package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.stream.Collector;

import org.aksw.jena_sparql_api.mapper.Accumulator;

public class ParallelAggregators {

	/**
	 * Create a java8 collector from a parallel aggregator.
	 * 
	 */
	public static <I, O, ACC extends Accumulator<I,O>> Collector<I, ?, O> createCollector(ParallelAggregator<I, O, ACC> agg) {
		return Collector.of(
			agg::createAccumulator,
			Accumulator::accumulate,
			agg::combine,
			Accumulator::getValue);
	}			

}
