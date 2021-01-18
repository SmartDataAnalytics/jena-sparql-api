package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregators;
import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder.SerializableSupplier;

public class AggNatural<I, C extends Collection<I>>
	implements ParallelAggregator<I, C, Accumulator<I, C>>, Serializable
{
	private static final long serialVersionUID = 3684074695846323687L;

	protected Supplier<? extends Accumulator<I, C>> accSupplier;
	
	public AggNatural(SerializableSupplier<? extends Accumulator<I, C>> accSupplier) {
		super();
		this.accSupplier = accSupplier;
	}

	@Override
	public Accumulator<I, C> createAccumulator() {
		return accSupplier.get();
	}

	@Override
	public Accumulator<I, C> combine(Accumulator<I, C> a, Accumulator<I, C> b) {
		return Aggregators.combineAccumulators(a, b, x -> x, x -> x);
	}
}
