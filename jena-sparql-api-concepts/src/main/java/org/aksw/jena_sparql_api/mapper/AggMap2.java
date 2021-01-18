package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Aggregator that maps each input item to a single key and allocates
 * a sub aggregator for that key if none exists yet.
 * 
 * @author raven
 *
 * @param <B>
 * @param <K>
 * @param <V>
 * @param <C>
 */
public class AggMap2<B, K, V, C extends Aggregator<B, V>>
    implements Aggregator<B, Map<K,V>>
{
    private BiFunction<B, Long, K> mapper;
    private C subAgg;

    public AggMap2(BiFunction<B, Long, K> mapper, C subAgg) {
        this.mapper = mapper;
        this.subAgg = subAgg;
    }

    @Override
    public Accumulator<B, Map<K, V>> createAccumulator() {
        Accumulator<B, Map<K, V>> result = new AccMap2<B, K, V, C>(mapper, subAgg);
        return result;
    }

    public static <B, K, V, C extends Aggregator<B, V>> AggMap2<B, K, V, C> create(Function<B, K> mapper, C subAgg) {
        BiFunction<B, Long, K> fn = (binding, rowNum) -> mapper.apply(binding);
        AggMap2<B, K, V, C> result = create(fn, subAgg);
        return result;
    }

    public static <B, K, V, C extends Aggregator<B, V>> AggMap2<B, K, V, C> create(BiFunction<B, Long, K> mapper, C subAgg) {
        AggMap2<B, K, V, C> result = new AggMap2<B, K, V, C>(mapper, subAgg);
        return result;
    }
}
