package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * 
 * 
 * @author raven
 *
 * @param <B>
 * @param <K>
 * @param <V>
 * @param <C>
 */
public class AccMap2<B, K, V, C extends Aggregator<B, V>>
    implements Accumulator<B, Map<K, V>>
{
    protected BiFunction<B, Long, K> mapper;
    protected C subAgg;

    protected Map<K, Accumulator<B, V>> state = new HashMap<>();

    public AccMap2(Function<B, K> mapper, C subAgg) {
        this((binding, rowNum) -> mapper.apply(binding), subAgg);
    }

    public AccMap2(BiFunction<B, Long, K> mapper, C subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public void accumulate(B binding) {
        // TODO Keep track of the relative binding index
        K k = mapper.apply(binding, -1l);
        Accumulator<B, V> subAcc = state.get(k);
        if(subAcc == null) {
            subAcc = subAgg.createAccumulator();
            state.put(k, subAcc);
        }
        subAcc.accumulate(binding);
    }

    @Override
    public Map<K, V> getValue() {
        Map<K, V> result = new HashMap<K, V>();

        for(Entry<K, Accumulator<B, V>> entry : state.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue().getValue();

            result.put(k, v);
        }

        return result;
    }

    public static <B, K, V, C extends Aggregator<B, V>> AccMap2<B, K, V, C> create(Function<B, K> mapper, C subAgg) {
        BiFunction<B, Long, K> fn = (binding, rowNum) -> mapper.apply(binding);
        AccMap2<B, K, V, C> result = new AccMap2<>(fn, subAgg);
        return result;
    }

    public static <B, K, V, C extends Aggregator<B, V>> AccMap2<B, K, V, C> create(BiFunction<B, Long, K> mapper, C subAgg) {
        AccMap2<B, K, V, C> result = new AccMap2<>(mapper, subAgg);
        return result;
    }

}
