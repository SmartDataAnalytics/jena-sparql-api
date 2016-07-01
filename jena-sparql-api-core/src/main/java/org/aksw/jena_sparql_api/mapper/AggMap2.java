package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.function.BiFunction;

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

    public static <B, K, V, C extends Aggregator<B, V>> AggMap2<B, K, V, C> create(BiFunction<B, Long, K> mapper, C subAgg) {
        AggMap2<B, K, V, C> result = new AggMap2<B, K, V, C>(mapper, subAgg);
        return result;
    }

}
