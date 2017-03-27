package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import org.apache.jena.sparql.engine.binding.Binding;

public class AccTransform<I, O>
    extends AccTransform2<Binding, I, O>
    implements Acc<O>
{
    public AccTransform(Accumulator<Binding, I> subAcc, Function<I, O> transform) {
        super(subAcc, transform);
    }
}

//public class AccTransform<I, O>
//    implements Acc<O>
//{
//    private Acc<I> subAcc;
//    private Function<I, O> transform;
//
//    public AccTransform(Acc<I> subAcc, Function<I, O> transform) {
//        this.subAcc = subAcc;
//        this.transform = transform;
//    }
//
//    @Override
//    public void accumulate(Binding binding) {
//        subAcc.accumulate(binding);
//    }
//
//    @Override
//    public O getValue() {
//        I input = subAcc.getValue();
//        O result = transform.apply(input);
//        return result;
//    }
//
//}
