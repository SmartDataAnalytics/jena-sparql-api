package org.aksw.jena_sparql_api.view_matcher;

import java.util.function.BiFunction;

public class GenericBinaryOpImpl<I, O>
    implements GenericBinaryOp<O>
{
    protected BiFunction<? super I, ? super I, O> delegate;

    public GenericBinaryOpImpl(BiFunction<? super I, ? super I, O> delegate) {
        super();
        this.delegate = delegate;
    }

    public O apply(Object a, Object b) {
        @SuppressWarnings("unchecked")
        I _a = (I)a;
        @SuppressWarnings("unchecked")
        I _b = (I)b;

        //O result = apply(_a, _b);
        O result = delegate.apply(_a, _b);
        return result;
    }

    public static <I, O> GenericBinaryOpImpl<I, O> create(BiFunction<? super I, ? super I, O> delegate) {
        GenericBinaryOpImpl<I, O> result = new GenericBinaryOpImpl<>(delegate);
        return result;
    }
}
