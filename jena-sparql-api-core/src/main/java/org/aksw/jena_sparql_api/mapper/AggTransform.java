package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Var;

public class AggTransform<I, O>
    implements Agg<O>
{
    private Agg<I> subAgg;
    private Function<I, O> transform;

    public AggTransform(Agg<I> subAgg, Function<I, O> transform) {
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

    public static <I, O> AggTransform<I, O> create(Agg<I> subAgg, Function<I, O> transform) {
        AggTransform<I, O> result = new AggTransform<I, O>(subAgg, transform);
        return result;
    }

}