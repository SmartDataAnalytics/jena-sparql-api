package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.stream.Collector;

import org.aksw.jena_sparql_api.mapper.Accumulator;

public class ParallelAggregators {

	/**
	 * Create a serializable java8 collector from a parallel aggregator.
	 * 
	 */
	public static <I, O, ACC extends Accumulator<I,O>> Collector<I, ?, O> createCollector(ParallelAggregator<I, O, ACC> agg) {
		return SerializableCollectorImpl.create(
				agg::createAccumulator,
				Accumulator::accumulate, 
				agg::combine,
				Accumulator::getValue);
	}
}
