package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;


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
public class AccMultiplexDynamic<B, K, V, W, C extends Aggregator<V, W>>
    implements Accumulator<B, Map<K, W>>
{
	// Map a binding to a set of keys; only unique items are used from the iterable
    protected Function<? super B, ? extends Iterator<? extends K>> keyMapper;
    
    // Map (binding, key) to value
    protected BiFunction<? super B, ? super K, ? extends V> valueMapper;
    
    // Note: Above functions are equivalent to the one below, but then a Map needs to be enforced
	// protected Function<? super B, ? extends Map<? extends K, ? extends V>> bindingToMap;
	
    protected C subAgg;

    protected Map<K, Accumulator<V, W>> state = new HashMap<>();

//    public AccMultiplexDynamic(Function<? super B, ? extends Iterable<? extends K>> mapper, C subAgg) {
//        this((binding, rowNum) -> mapper.apply(binding), subAgg);
//    }

    public AccMultiplexDynamic(
    		Function<? super B, ? extends Iterator<? extends K>> keyMapper,
    		BiFunction<? super B, ? super K, ? extends V> valueMapper,
    		C subAgg) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.subAgg = subAgg;
    }

    @Override
    public void accumulate(B binding) {
        // TODO Keep track of the relative binding index
        Iterator<? extends K> ks = keyMapper.apply(binding);
 
        while (ks.hasNext()) {
        	K k = ks.next();
        	V v = valueMapper.apply(binding, k);

 	        Accumulator<V, W> subAcc = state.get(k);
	        if(subAcc == null) {
	            subAcc = subAgg.createAccumulator();
	            state.put(k, subAcc);
	        }
	        subAcc.accumulate(v);
        }
    }

    @Override
    public Map<K, W> getValue() {
        Map<K, W> result = new HashMap<K, W>();

        for(Entry<K, Accumulator<V, W>> entry : state.entrySet()) {
            K k = entry.getKey();
            W v = entry.getValue().getValue();

            result.put(k, v);
        }

        return result;
    }

    public static <B, K, V, W, C extends Aggregator<V, W>> AccMultiplexDynamic<B, K, V, W, C> create(
    		Function<? super B, ? extends Iterator<? extends K>> keyMapper,
    		BiFunction<? super B, ? super K, ? extends V> valueMapper,
    		C subAgg) {
        AccMultiplexDynamic<B, K, V, W, C> result = new AccMultiplexDynamic<>(keyMapper, valueMapper, subAgg);
        return result;
    }

//	public static Object create(Function<? super B, ? extends Iterator<? extends K>> keyMapper2,
//			BiFunction<? super B, ? super K, ? extends T> valueMapper2, Aggregator<B, T> local) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//    public static <B, K, V, C extends Aggregator<B, V>> AccMultiplexDynamic<B, K, V, C> create(BiFunction<? super B, Long, ? extends Iterable<? extends K>> mapper, C subAgg) {
//        AccMultiplexDynamic<B, K, V, C> result = new AccMultiplexDynamic<>(mapper, subAgg);
//        return result;
//    }

}
