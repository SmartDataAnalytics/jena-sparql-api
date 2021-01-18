package org.aksw.jena_sparql_api.mapper.parallel;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.Accumulator;

//public class ParallelAggregatorWrapper<B, V, A extends Accumulator<B, V>>
//	implements ParallelAggregator<B, V>
//{
//	protected Supplier<A> accSupplier;
//	protected BinaryOperator<Accumulator<B, V>> combiner;
//	
//	public ParallelAggregatorWrapper(Supplier<A> accSupplier, BinaryOperator<Accumulator<B, V>> combiner) {
//		super();
//		this.accSupplier = accSupplier;
//		this.combiner = combiner;
//	}
//
//	@Override
//	public Accumulator<B, V> createAccumulator() {
//		return accSupplier.get();
//	}
//
//	@Override
//	public Accumulator<B, V> combine(Accumulator<B, V> a, Accumulator<B, V> b) {
//		Accumulator<B, V> result = combiner.apply(a, b);
//		return result;
//	}
//	
//	
//}
