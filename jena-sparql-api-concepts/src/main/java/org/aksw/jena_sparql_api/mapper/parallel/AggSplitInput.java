package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggSplitInput.AccSplitInput;

import com.google.common.collect.Sets;


//class AccSplitInput<I, K, J, O, SUBACC extends Accumulator<J, O>>
//	implements Accumulator<I, SUBACC>, Serializable
//{
//	// AggSplitInput
//	protected Map<K, SUBACC> keyToSubAcc;
//	
//	// protected SUBACC subAcc;
//	protected Function<? super I, ? extends J> inputTransform;
//	
//	public AccSplitInput(SUBACC subAcc, Function<? super I, ? extends J> inputTransform) {
//		super();
//		this.subAcc = subAcc;
//	}
//	
//	@Override
//	public void accumulate(I input) {
//		J transformedInput = inputTransform.apply(input);
//		subAcc.accumulate(transformedInput);
//	}
//	
//	@Override
//	public SUBACC getValue() {
//		return subAcc;
//	}		
//}


/**
 * An aggregator that splits the index into a set of keys and forwards the input to the accumulators
 * for each key
 * 
 * @author raven
 *
 * @param <I>
 * @param <K>
 * @param <O>
 * @param <SUBACC>
 * @param <SUBAGG>
 */
public class AggSplitInput<I, K, J, O,
	SUBACC extends Accumulator<J, O>, SUBAGG extends ParallelAggregator<J, O, SUBACC>>
	implements ParallelAggregator<I, Map<K, O>, AccSplitInput<I, K, J, O, SUBACC>>,
		Serializable
{
	private static final long serialVersionUID = 7584075431975571180L;


	public static interface AccSplitInput<I, K, J, O, SUBACC extends Accumulator<J, O>>
		extends AccWrapper<I, Map<K, O>, Map<K, SUBACC>> {
		}

	
	
	// Map a binding to a set of keys; only unique items are used from the iterable
	protected Function<? super I, ? extends Set<? extends K>> keyMapper;

    protected BiFunction<? super I, ? super K, ? extends J> valueMapper;

	protected SUBAGG subAgg;
	
	public AggSplitInput(SUBAGG subAgg,
			Function<? super I, ? extends Set<? extends K>> keyMapper,
			BiFunction<? super I, ? super K, ? extends J> valueMapper) {
		super();
		this.subAgg = subAgg;
		this.keyMapper = keyMapper;
		this.valueMapper = valueMapper;
	}
	
	@Override
	public AccSplitInput<I, K, J, O, SUBACC> createAccumulator() {
		return new AccSplitInputImpl(new LinkedHashMap<>());
	}
	
	@Override
	public AccSplitInput<I, K, J, O, SUBACC> combine(AccSplitInput<I, K, J, O, SUBACC> a,
			AccSplitInput<I, K, J, O, SUBACC> b) {
		Map<K, SUBACC> accA = a.getSubAcc();
		Map<K, SUBACC> accB = b.getSubAcc();
		
		
		Map<K, SUBACC> newMap = new LinkedHashMap<>();
		
		Set<K> allKeys = Sets.union(accA.keySet(), accB.keySet());
		for (K key : allKeys) {
			SUBACC subAccA = accA.get(key);
			SUBACC subAccB = accB.get(key);
			
			SUBACC combined;
			if (subAccA != null) {
				if (subAccB != null) {
					combined = subAgg.combine(subAccA, subAccB);
				} else {
					combined = subAccA;
				}
			} else {
				if (subAccB != null) {
					combined = subAccB;
				} else {
					// Both accs are null - should never happen
					throw new RuntimeException("Combination of two null accumulators - should never happen");
				}
			}
			
			newMap.put(key, combined);
		}

		
		return new AccSplitInputImpl(newMap); 
	}
	

	public class AccSplitInputImpl
		implements AccSplitInput<I, K, J, O, SUBACC>, Serializable
	{
		private static final long serialVersionUID = 871477289930122459L;

		protected Map<K, SUBACC> keyToSubAcc;
		
		
		
		public AccSplitInputImpl(Map<K, SUBACC> keyToSubAcc) {
			super();
			this.keyToSubAcc = keyToSubAcc;
		}

		@Override
		public void accumulate(I input) {
			Set<? extends K> keys = keyMapper.apply(input);
			for (K key : keys) {
				
				SUBACC subAcc = keyToSubAcc.computeIfAbsent(key, k -> subAgg.createAccumulator());

				J newInput = valueMapper.apply(input, key);
				subAcc.accumulate(newInput);
			}
		}

		@Override
		public Map<K, O> getValue() {
			Map<K, O> result = keyToSubAcc.entrySet().stream()
				.map(e -> new SimpleEntry<>(e.getKey(), e.getValue().getValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, LinkedHashMap::new));
			
			return result;
		}

		@Override
		public Map<K, SUBACC> getSubAcc() {
			return keyToSubAcc;
		}
		
	}
}


/**
 * A more general variant of AccMap2 which maps a binding to multiple keys
 * 
 * TODO Decide whether to keep only this general version even
 *      if it means having to wrap each key as a singleton collection
 * 
 * 
 * @author raven
 *
 * @param <B>
 * @param <K>
 * @param <V>
 * @param <C>
 */
//public class AggSplitInput<B, K, V, W extends ParallelAggregator<V, W>>
//    implements ParallelAggregator<B, Map<K, W>>
//{
//	// Map a binding to a set of keys; only unique items are used from the iterable
//    protected Function<? super B, ? extends Iterator<? extends K>> keyMapper;
//    
//    // Map (binding, key) to value
//    protected BiFunction<? super B, ? super K, ? extends V> valueMapper;
//    
//    // Note: Above functions are equivalent to the one below, but then a Map needs to be enforced
//	// protected Function<? super B, ? extends Map<? extends K, ? extends V>> bindingToMap;
//	
//    protected W subAgg;
//
//    protected Map<K, Accumulator<V, W>> state = new HashMap<>();
//
//	@Override
//	public Accumulator<B, Map<K, W>> createAccumulator() {
//		return new AccMultiplexDynamic<>(keyMapper, valueMapper, subAgg);
//	}
//
//	@Override
//	public Accumulator<B, Map<K, W>> combine(Accumulator<B, Map<K, W>> a, Accumulator<B, Map<K, W>> b) {
//		for (
//		
//	}
//
////    public AccMultiplexDynamic(Function<? super B, ? extends Iterable<? extends K>> mapper, C subAgg) {
////        this((binding, rowNum) -> mapper.apply(binding), subAgg);
////    }
//    
//    
//    
//
////    public ParallelAggMultiplexDynamic(
////    		Function<? super B, ? extends Iterator<? extends K>> keyMapper,
////    		BiFunction<? super B, ? super K, ? extends V> valueMapper,
////    		C subAgg) {
////        this.keyMapper = keyMapper;
////        this.valueMapper = valueMapper;
////        this.subAgg = subAgg;
////    }
////
////    @Override
////    public void accumulate(B binding) {
////        // TODO Keep track of the relative binding index
////        Iterator<? extends K> ks = keyMapper.apply(binding);
//// 
////        while (ks.hasNext()) {
////        	K k = ks.next();
////        	V v = valueMapper.apply(binding, k);
////
//// 	        Accumulator<V, W> subAcc = state.get(k);
////	        if(subAcc == null) {
////	            subAcc = subAgg.createAccumulator();
////	            state.put(k, subAcc);
////	        }
////	        subAcc.accumulate(v);
////        }
////    }
////
////    @Override
////    public Map<K, W> getValue() {
////        Map<K, W> result = new HashMap<K, W>();
////
////        for(Entry<K, Accumulator<V, W>> entry : state.entrySet()) {
////            K k = entry.getKey();
////            W v = entry.getValue().getValue();
////
////            result.put(k, v);
////        }
////
////        return result;
////    }
////
////    public static <B, K, V, W, C extends Aggregator<V, W>> AccMultiplexDynamic<B, K, V, W, C> create(
////    		Function<? super B, ? extends Iterator<? extends K>> keyMapper,
////    		BiFunction<? super B, ? super K, ? extends V> valueMapper,
////    		C subAgg) {
////        AccMultiplexDynamic<B, K, V, W, C> result = new AccMultiplexDynamic<>(keyMapper, valueMapper, subAgg);
////        return result;
////    }
//
////	public static Object create(Function<? super B, ? extends Iterator<? extends K>> keyMapper2,
////			BiFunction<? super B, ? super K, ? extends T> valueMapper2, Aggregator<B, T> local) {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
////    public static <B, K, V, C extends Aggregator<B, V>> AccMultiplexDynamic<B, K, V, C> create(BiFunction<? super B, Long, ? extends Iterable<? extends K>> mapper, C subAgg) {
////        AccMultiplexDynamic<B, K, V, C> result = new AccMultiplexDynamic<>(mapper, subAgg);
////        return result;
////    }
//
//}
