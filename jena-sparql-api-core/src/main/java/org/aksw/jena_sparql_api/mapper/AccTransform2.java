package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

public class AccTransform2<B, I, O>
    implements Accumulator<B, O>
{
    protected Accumulator<B, I> subAcc;
    protected Function<? super I, O> transform;

    public AccTransform2(Accumulator<B, I> subAcc, Function<? super I, O> transform) {
        this.subAcc = subAcc;
        this.transform = transform;
    }

    @Override
    public void accumulate(B binding) {
        subAcc.accumulate(binding);
    }

    @Override
    public O getValue() {
        I input = subAcc.getValue();
        O result = transform.apply(input);
        return result;
    }

    public static <B, I, O> Accumulator<B, O> create(Accumulator<B, I> subAcc, Function<? super I, O> transform) {
        Accumulator<B, O> result = create(subAcc, transform);
        return result;
    }

}
