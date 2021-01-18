package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregators;

public interface NaturalParallelAggregator<T, C extends Collection<T>>
	extends ParallelAggregator<T, C, Accumulator<T, C>>
{
	@Override
	default Accumulator<T, C> combine(Accumulator<T, C> a, Accumulator<T, C> b) {
		Accumulator<T, C> result = Aggregators.combineAccumulators(a, b, x -> x, x -> x);
		return result;
	}
}
