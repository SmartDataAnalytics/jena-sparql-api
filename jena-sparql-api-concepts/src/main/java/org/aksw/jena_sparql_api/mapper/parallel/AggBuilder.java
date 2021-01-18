package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.Aggregator;
import org.aksw.jena_sparql_api.mapper.parallel.AggFilterInput.AccFilterInput;
import org.aksw.jena_sparql_api.mapper.parallel.AggSplitInput.AccSplitInput;
import org.aksw.jena_sparql_api.mapper.parallel.AggTransformInput.AccTransformInput;

/**
 * Builder for parallel aggregators.
 * 
 * Static 'from' methods start the builder chain.
 * All methods that performa modifications return a new independent builder object.
 * Because type expressions can become complex there are three getters that return the
 * wrapped aggregator either fully typed, as a parallel aggregator or as a simple aggregator:
 * {@link #getFullyTyped()}, {@link #getAsParallelAggregator()}, {@link #getAsAggregator()}. 
 * 
 * 
 * @author raven
 *
 * @param <I> The current aggregator's input type
 * @param <O>  The current aggregator's output type
 * @param <ACC> The current aggregator's accumulator type
 * @param <AGG> The current aggregator's own type
 */
public class AggBuilder<I, O, ACC extends Accumulator<I, O>, AGG extends ParallelAggregator<I, O, ACC>> {
	
	public static interface SerializableCollector<T, A, R> extends Collector<T, A, R>, Serializable {}
	public static interface SerializablePredicate<T> extends Predicate<T>, Serializable {}
	public static interface SerializableSupplier<T> extends Supplier<T>, Serializable {}
	public static interface SerializableFunction<I, O> extends Function<I, O>, Serializable {}
	public static interface SerializableBiFunction<I1, I2, O> extends BiFunction<I1, I2, O>, Serializable {}
	
	
	protected AGG state;
	
	public AggBuilder(AGG state) {
		super();
		this.state = state;
	}

	public AGG getFullyTyped() {
		return state;
	}

	public ParallelAggregator<I, O, ?> getAsParallelAggregator() {
		return state;
	}

	public Aggregator<I, O> getAsAggregator() {
		return state;
	}

	
	public static <I, O,
					ACC extends Accumulator<I, O>,
					AGG extends ParallelAggregator<I, O, ACC>>
		AggBuilder<I, O, ACC, AGG> from(AGG agg)
	{
		return new AggBuilder<>(agg);
	}

	public static <T, C extends Collection<T>>
	AggBuilder<T, C, Accumulator<T, C>, ParallelAggregator<T, C, Accumulator<T, C>>> fromNaturalAccumulator(SerializableSupplier<? extends Accumulator<T, C>> accSupplier)
	{
		return from(new AggNatural<>(accSupplier));
	}

	public AggBuilder<I, O, AccFilterInput<I, O, ACC>, AggFilterInput<I, O, ACC, AGG>> withInputFilter(SerializablePredicate<? super I> inputFilter) {
		 return from(new AggFilterInput<>(state, inputFilter));
	}

	public <H> AggBuilder<H, O, AccTransformInput<H, I, O, ACC>, AggTransformInput<H, I, O, ACC, AGG>>
		withInputTransform(SerializableFunction<? super H, ? extends I> inputTransform) {
		return from(new AggTransformInput<>(state, inputTransform));
	}

	public <H, K> AggBuilder<H, Map<K, O>, AccSplitInput<H, K, I, O, ACC>, AggSplitInput<H, K, I, O, ACC, AGG>> withInputSplit(
			SerializableFunction<? super H, ? extends Set<? extends K>> keyMapper,
			SerializableBiFunction<? super H, ? super K, ? extends I> valueMapper			
	) {
		return from(new AggSplitInput<>(state, keyMapper, valueMapper));
	}
	

}


