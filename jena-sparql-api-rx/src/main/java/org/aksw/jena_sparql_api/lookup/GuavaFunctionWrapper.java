package org.aksw.jena_sparql_api.lookup;

import java.util.function.Function;

public class GuavaFunctionWrapper<I, O>
    implements com.google.common.base.Function<I, O>
{
    protected Function<I, O> fn;

    public GuavaFunctionWrapper(Function<I, O> fn) {
        super();
        this.fn = fn;
    }

    @Override
    public O apply(I input) {
        O result = fn.apply(input);
        return result;
    }

    public static <I, O> com.google.common.base.Function<I, O> wrap(Function<I, O> fn) {
        GuavaFunctionWrapper<I, O> result = new GuavaFunctionWrapper<>(fn);
        return result;
    }
}
