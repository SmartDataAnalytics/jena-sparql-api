package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

public class AccBindingTransform<B, V, U>
    implements Accumulator<B, V>
{
    protected Function<? super B, U> transform;
    protected Accumulator<? super U, V> subAcc;

    public AccBindingTransform(Function<? super B, U> transform, Accumulator<? super U, V> subAcc) {
        super();
        this.transform = transform;
        this.subAcc = subAcc;
    }

    @Override
    public void accumulate(B binding) {
        U u = transform.apply(binding);
        subAcc.accumulate(u);;
    }

    @Override
    public V getValue() {
        V result = subAcc.getValue();
        return result;
    }

    public static <B, V, U> Accumulator<B, V> create(Function<? super B, U> transform, Accumulator<? super U, V> subAcc) {
        Accumulator<B, V>  result = new AccBindingTransform<>(transform, subAcc);
        return result;
    }

}
