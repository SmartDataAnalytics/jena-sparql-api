package org.aksw.jena_sparql_api.mapper.parallel;


import java.io.Serializable;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggInputFilter.AccInputFilter;

/**
 * Wrap an aggregator such that inputs are passed through a predicate.
 * The predicate must evaluate to true for an item to be forwarded to the underyling accumulator.
 * 
 * @author raven
 *
 * @param <I>
 * @param <O>
 * @param <SUBACC>
 * @param <SUBAGG>
 */
public class AggInputFilter<
		I,
		O,
		SUBACC extends Accumulator<I, O>,
		SUBAGG extends ParallelAggregator<I, O, SUBACC>
	>
	implements ParallelAggregator<I, O, AccInputFilter<I, O, SUBACC>>, Serializable
{
	private static final long serialVersionUID = 7915920812328084498L;

	public static interface AccInputFilter<I, O, SUBACC extends Accumulator<I, O>>
		extends AccWrapper<I, O, SUBACC> {
	}

	protected SUBAGG subAgg;
	protected Predicate<? super I> inputFilter;
	
	public AggInputFilter(SUBAGG subAgg, Predicate<? super I> inputFilter) {
		super();
		this.subAgg = subAgg;
		this.inputFilter = inputFilter;
	}

	@Override
	public AccInputFilter<I, O, SUBACC> createAccumulator() {
		SUBACC subAcc = subAgg.createAccumulator();
		
		return new AccFilterInputImpl(subAcc, inputFilter);
	}

	@Override
	public AccInputFilter<I, O, SUBACC> combine(AccInputFilter<I, O, SUBACC> a,
			AccInputFilter<I, O, SUBACC> b) {
//		SUBACC accA = a.getValue();
//		SUBACC accB = b.getValue();
		SUBACC accA = a.getSubAcc();
		SUBACC accB = b.getSubAcc();
		SUBACC combined = subAgg.combine(accA, accB);
		
		return new AccFilterInputImpl(combined, inputFilter); 
	}
	
//	@Override
//	public O getValue(AccFilterInput<I, O, SUBACC> a) {
//		return subAgg.getValue(a.getValue());
//	}

	
	public class AccFilterInputImpl
		implements AccInputFilter<I, O, SUBACC>, Serializable
	{		
		private static final long serialVersionUID = 6300861998024026360L;
	
		protected SUBACC subAcc;
		protected Predicate<? super I> inputFilter;
		
		public AccFilterInputImpl(SUBACC subAcc, Predicate<? super I> inputFilter) {
			super();
			this.subAcc = subAcc;
			this.inputFilter = inputFilter;
		}
		
		@Override
		public void accumulate(I input) {
			boolean isAccepted = inputFilter.test(input);
			
			if (isAccepted) {
				subAcc.accumulate(input);
			}
		}
		
//		@Override
//		public SUBACC getValue() {
//			return subAcc.getValue();
//		}		

		@Override
		public SUBACC getSubAcc() {
			return subAcc;
		}		

		@Override
		public O getValue() {
			return subAcc.getValue();
		}		
	}
}
