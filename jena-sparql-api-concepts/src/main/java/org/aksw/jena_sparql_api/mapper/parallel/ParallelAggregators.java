package org.aksw.jena_sparql_api.mapper.parallel;

public class ParallelAggregators {
	
	/**
	 * Turn a plain aggregator (i.e. supplier of accumulator) into a parallel one
	 * by providing a combiner function 
	 * 
	 * @return
	 */
	// TODO
	
	
	/**
	 * Turn an Aggregator&getB, Map&gt;K, V&lt;&lt; into a parallel one
	 * 
	 * 
	 * by providing a combine function on the values.
	 * 
	 * @param <B>
	 * @param <K>
	 * @param <V>
	 * @param <C>
	 * @param accSupplier
	 * @return
	 */
//	public static <B, K, V, A extends Accumulator<B, Map<K, W>> ParallelAggregator<B, W> makeParallel(
//			Supplier<A>> accSupplier) {
//		return new ParallelAggregator<B, C>() {
//			@Override
//			public Accumulator<B, Map<K, C>> createAccumulator() { 
//				return accSupplier.get();
//			}
//			
//			@Override
//			public Accumulator<B, C> combine(Accumulator<B, C> a, Accumulator<B, C> b) {
//				return Aggregators.combineAccumulators(a, b, x -> x, x -> x);
//			}
//		};
//	}

	
	/**
	 * Wrap an accumulator for T -&gt; Collection&lt;T$gt; with a generic combine function.
	 * 
	 * All items of one of the accumulated collections are passed to the accumulate method of the
	 * other accumulator (or a fresh on).
	 * 
	 * @param <B>
	 * @param <C>
	 * @param accSupplier
	 * @return
	 */
//	public static <B, C extends Collection<B>> ParallelAggregator<B, C> makeParallel(Supplier<? extends Accumulator<B, C>> accSupplier) {
//		return new ParallelAggregator<B, C>() {
//			@Override
//			public Accumulator<B, C> createAccumulator() { 
//				return accSupplier.get();
//			}
//			
//			@Override
//			public Accumulator<B, C> combine(Accumulator<B, C> a, Accumulator<B, C> b) {
//				return Aggregators.combineAccumulators(a, b, x -> x, x -> x);
//			}
//		};
//	}

	
	/**
	 * Make an aggregator parallel by providing a custom combiner function for the accumulator's
	 * values.
	 * 
	 * @param <B>
	 * @param <V>
	 * @param accSupplier
	 * @param combiner
	 * @return
	 */
//	public <B, V> ParallelAggregator<B, V> makeParallel(
//			Supplier<? extends Accumulator<B, V>> accSupplier, BinaryOperator<Accumulator<B, V>> combiner) {
//		return new ParallelAggregatorWrapper<>(accSupplier, combiner);
//	}
}
