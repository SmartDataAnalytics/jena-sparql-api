package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class AggregatorBuilder<B, T> {

    protected Aggregator<B, T> state;

    public AggregatorBuilder(Aggregator<B, T> state) {
        super();
        this.state = state;
    }

    public Aggregator<B, T> get() {
        return state;
    }

    public <K> AggregatorBuilder<B, Map<K, T>> wrapWithMap(Function<B, K> bindingToKey) {
        Aggregator<B, Map<K, T>> agg = AggMap2.create(bindingToKey, state);

        return new AggregatorBuilder<>(agg);
    }

    public <O> AggregatorBuilder<B, O> wrapWithTransform(Function<? super T, O> transform) {
        Aggregator<B, O> agg = AggTransform2.create(state, transform);

        return new AggregatorBuilder<>(agg);
    }

    public AggregatorBuilder<B, T> wrapWithCondition(Predicate<B> predicate) {
        // TODO Is this correct??? i.e. calling createAccumulator here
        Aggregator<B, T> local = state;
        Aggregator<B, T> agg = () -> AccCondition.create(predicate, local.createAccumulator());

        return new AggregatorBuilder<>(agg);
    }

    public <U> AggregatorBuilder<U, T> wrapWithBindingTransform(Function<? super U, B> transform) {
        Aggregator<B, T> local = state;
        Aggregator<U, T> agg = () -> AccBindingTransform.create(transform, local.createAccumulator());

        return new AggregatorBuilder<>(agg);
    }

//    public static <B, T> AggregatorBuilder<B, T> from(Supplier<Accumulator<B, T>> accSupplier) {
//        Aggregator<B, T> agg = () -> accSupplier.get();
//
//        return new AggregatorBuilder<>(agg);
//    }

    public static <B, T> AggregatorBuilder<B, T> from(Aggregator<B, T> agg) {
        return new AggregatorBuilder<>(agg);
    }

    // combine: BiFunction<I, I> -> T
//    public static <B, T> AggregatorBuilder<B, T> from(Aggregator<B, ? extends T> a, Aggregator<B, ? extends T> b, BiFunction<? super T, ? super T, T> combiner) {
//    	Aggregator<B, T> agg = () -> {
//    		a.createAccumulator();
//    		b.createAccumulator();
//    	}
//    }
}