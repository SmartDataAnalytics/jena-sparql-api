package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggOutputTransform.AccOutputTransform;


public class AggOutputTransform<I, O, P,
		SUBACC extends Accumulator<I, O>,
		SUBAGG extends ParallelAggregator<I, O, SUBACC>
	>
	implements ParallelAggregator<I, P, AccOutputTransform<I, O, P, SUBACC>>, Serializable
{
	private static final long serialVersionUID = 7390091141161262048L;

	public static interface AccOutputTransform<I, O, P, SUBACC extends Accumulator<I, O>>
		extends AccWrapper<I, P, SUBACC> { }

	protected SUBAGG subAgg;
	protected Function<? super O, ? extends P> outputTransform;
	
	public AggOutputTransform(SUBAGG subAgg, Function<? super O, ? extends P> outputTransform) {
		super();
		this.subAgg = subAgg;
		this.outputTransform = outputTransform;
	}
	
	@Override
	public AccOutputTransform<I, O, P, SUBACC> createAccumulator() {
		SUBACC subAcc = subAgg.createAccumulator();
		
		return new AccOutputTransformImpl(subAcc, outputTransform);
	}
	
	@Override
	public AccOutputTransform<I, O, P, SUBACC> combine(AccOutputTransform<I, O, P, SUBACC> a,
			AccOutputTransform<I, O, P, SUBACC> b) {
		SUBACC accA = a.getSubAcc();
		SUBACC accB = b.getSubAcc();
		SUBACC combined = subAgg.combine(accA, accB);
		
		return new AccOutputTransformImpl(combined, outputTransform);
	}

	public class AccOutputTransformImpl
		implements AccOutputTransform<I, O, P, SUBACC>, Serializable
	{
		private static final long serialVersionUID = -8375605065935368095L;

		protected SUBACC subAcc;
		protected Function<? super O, ? extends P> outputTransform;
		
		public AccOutputTransformImpl(SUBACC subAcc, Function<? super O, ? extends P> outputTransform) {
			super();
			this.subAcc = subAcc;
			this.outputTransform = outputTransform;
		}
		
		@Override
		public void accumulate(I input) {
			subAcc.accumulate(input);
		}
	
		@Override
		public SUBACC getSubAcc() {
			return subAcc;
		}
		
		@Override
		public P getValue() {
			O rawResult = subAcc.getValue();
			P result = outputTransform.apply(rawResult);
			return result;
		}
	}
}
