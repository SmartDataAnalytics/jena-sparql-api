package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

public class AccTransform2<B, I, O>
    implements Accumulator<B, O>
{
    protected Accumulator<B, I> subAcc;
    protected Function<I, O> transform;

    public AccTransform2(Accumulator<B, I> subAcc, Function<I, O> transform) {
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

}
