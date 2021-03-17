package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

public class AggTransform2<B, I, O, C extends Aggregator<B, I>>
    implements Aggregator<B, O>
{
    private C subAgg;
    private Function<? super I, O> transform;

    @Deprecated
    public AggTransform2(C subAgg, com.google.common.base.Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = (arg) -> transform.apply(arg);
    }

    public AggTransform2(C subAgg, Function<? super I, O> transform) {
        this.subAgg = subAgg;
        this.transform = transform;
    }

    @Override
    public Accumulator<B, O> createAccumulator() {
        Accumulator<B, I> baseAcc = subAgg.createAccumulator();
        Accumulator<B, O> result = new AccTransform2<>(baseAcc, transform);
        return result;
    }

    public static <B, I, O, C extends Aggregator<B, I>> AggTransform2<B, I, O, C> create(C subAgg, Function<? super I, O> transform) {
        AggTransform2<B, I, O, C> result = new AggTransform2<>(subAgg, transform);
        return result;
    }

//    public static <I, O> AggTransform<I, O> create(Agg<I> subAgg, Function<I, O> transform) {
//        AggTransform<I, O> result = new AggTransform<I, O>(subAgg, transform);
//        return result;
//    }

}