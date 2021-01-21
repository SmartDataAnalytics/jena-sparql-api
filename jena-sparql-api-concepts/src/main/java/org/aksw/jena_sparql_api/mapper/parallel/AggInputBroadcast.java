package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggInputBroadcast.AccInputSplitStatic;

/**
 * An aggregator that broadcasts its input to two sub-aggregators that accept the same input.
 * 
 * Serves the purpose to perform two independent type safe aggregations on the same input in a single pass.
 * 
 * @author raven
 *
 * @param <I> Input type
 * @param <O1> output type of first aggregator
 * @param <O2> output type of second aggregator
 * @param <SUBACC1> accumulator type of first aggregator
 * @param <SUBAGG1> the type of the first aggregator
 * @param <SUBACC2> accumulator type of second aggregator
 * @param <SUBAGG2> the type of the second aggregator
 */
public class AggInputBroadcast<I, O1, O2,
	SUBACC1 extends Accumulator<I, O1>,
	SUBAGG1 extends ParallelAggregator<I, O1, SUBACC1>,
	SUBACC2 extends Accumulator<I, O2>,
	SUBAGG2 extends ParallelAggregator<I, O2, SUBACC2>
	>
	implements ParallelAggregator<I, Entry<O1, O2>, AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2>>, Serializable
{
	private static final long serialVersionUID = 9133934839769656122L;


	public static interface AccInputSplitStatic<I, O1, O2,
		SUBACC1 extends Accumulator<I, O1>,
		SUBACC2 extends Accumulator<I, O2>>
	extends Accumulator<I, Entry<O1, O2>> {
		SUBACC1 getSubAcc1();
		SUBACC2 getSubAcc2();
	}
	
	protected SUBAGG1 subAgg1;
	protected SUBAGG2 subAgg2;
	
	public AggInputBroadcast(SUBAGG1 subAgg1, SUBAGG2 subAgg2) {
		super();
		this.subAgg1 = subAgg1;
		this.subAgg2 = subAgg2;
	}
	
	@Override
	public AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2> createAccumulator() {
		return new AccInputSplitStaticImpl(subAgg1.createAccumulator(), subAgg2.createAccumulator());
	}
	
	@Override
	public AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2> combine(
			AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2> a,
			AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2> b) {

		SUBACC1 newSubAcc1 = subAgg1.combine(a.getSubAcc1(), b.getSubAcc1());
		SUBACC2 newSubAcc2 = subAgg2.combine(a.getSubAcc2(), b.getSubAcc2());
		
		return new AccInputSplitStaticImpl(newSubAcc1, newSubAcc2); 
	}


	public class AccInputSplitStaticImpl
		implements AccInputSplitStatic<I, O1, O2, SUBACC1, SUBACC2>, Serializable
	{
		private static final long serialVersionUID = 8282006564407136491L;
		
		// protected Map<K, SUBACC> keyToSubAcc;
		protected SUBACC1 subAcc1;
		protected SUBACC2 subAcc2;
		
		
		
		public AccInputSplitStaticImpl(SUBACC1 subAcc1, SUBACC2 subAcc2) {
			super();
			this.subAcc1 = subAcc1;
			this.subAcc2 = subAcc2;
		}
	
		@Override
		public void accumulate(I input) {
			subAcc1.accumulate(input);
			subAcc2.accumulate(input);
		}
	
		@Override
		public Entry<O1, O2> getValue() {
			O1 o1 = subAcc1.getValue();
			O2 o2 = subAcc2.getValue();
			
			return new SimpleEntry<>(o1, o2);
		}
	
		@Override
		public SUBACC1 getSubAcc1() {
			return subAcc1;
		}

		@Override
		public SUBACC2 getSubAcc2() {
			return subAcc2;
		}
		
	}
}
