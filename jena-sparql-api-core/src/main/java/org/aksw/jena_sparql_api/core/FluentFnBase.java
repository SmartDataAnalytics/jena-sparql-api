package org.aksw.jena_sparql_api.core;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * Abstract class upon Fluent APIs for functions can be built.
 *
 * Provides .compose() and default behavior for .create() and .end() (i.e. returns an identity function if fn is null)
 *
 * @author raven
 *
 * @param <T>
 * @param <P>
 */
public abstract class FluentFnBase<T, P>
    extends FluentBase<Function<T, T>, P>
{
    public FluentFnBase() {
        this(null);
    }

    public FluentFnBase(Function<T, T> fn) {
        super(fn);
    }

    public FluentFnBase<T, P> compose(Function<T, T> nextFn) {
        if(fn == null) {
            fn = nextFn;
        } else {
            fn = Functions.compose(nextFn, fn);
        }

        return this;
    }

    @Override
    public Function<T, T> value() {
        Function<T, T> result = super.value();
        if(result == null) {
            result = Functions.<T>identity();
        }

        return result;
    }
}