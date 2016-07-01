package org.aksw.jena_sparql_api.mapper;

import java.util.Set;
import java.util.function.Function;

import org.apache.jena.sparql.core.Var;

public class AggTransform2<I, O, C extends Agg<I>>
    implements Agg<O>
{
    private C subAgg;
    private Function<I, O> transform;

    public AggTransform2(C subAgg, com.google.common.base.Function<I, O> transform) {
        this.subAgg = subAgg;
        this.transform = (arg) -> transform.apply(arg);
    }

    public AggTransform2(C subAgg, Function<I, O> transform) {
        this.subAgg = subAgg;
        this.transform = transform;
    }

    @Override
    public Acc<O> createAccumulator() {
        Acc<I> baseAcc = subAgg.createAccumulator();
        Acc<O> result = new AccTransform<I, O>(baseAcc, transform);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        Set<Var> result = subAgg.getDeclaredVars();
        return result;
    }

    public static <I, O, C extends Agg<I>> AggTransform2<I, O, C> create(C subAgg, com.google.common.base.Function<I, O> transform) {
        AggTransform2<I, O, C> result = new AggTransform2<I, O, C>(subAgg, transform);
        return result;
    }

//    public static <I, O> AggTransform<I, O> create(Agg<I> subAgg, Function<I, O> transform) {
//        AggTransform<I, O> result = new AggTransform<I, O>(subAgg, transform);
//        return result;
//    }

}