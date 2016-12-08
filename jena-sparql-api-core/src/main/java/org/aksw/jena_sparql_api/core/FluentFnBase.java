package org.aksw.jena_sparql_api.core;

import java.util.function.Function;

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
    /**
     * Controls chaining order when composing functions.
     * As a rule of thumb:
     * If the fluent successively wraps an object, reverse the chain direction, so that first fluent call corresponds to the last wrapper on which methods are invoked first.
     * If a fluent keeps replacing an object with a new one (i.e. no wrapping), chaining can be done in order, so that the last fluent call corresponds to the last replacement
     */
    protected boolean defaultReverseChaining;

    public FluentFnBase(boolean reverseChaining) {
        this(null, reverseChaining);
    }

    public FluentFnBase(Function<T, T> fn, boolean defaultReverseChaining) {
        super(fn);
        this.defaultReverseChaining = defaultReverseChaining;
    }

    public FluentFnBase<T, P> compose(Function<T, T> nextFn) {
        if(fn == null) {
            fn = nextFn;
        } else {
            fn = defaultReverseChaining
                ? fn.andThen(nextFn)
                : nextFn.andThen(fn)
                ;
            //fn = Functions.compose(nextFn, fn);
            //nextFn.andThen(fn);//Functions.compose(fn, nextFn);
            //fn = nextFn.andThen(fn);
        }

        return this;
    }

    @Override
    public Function<T, T> value() {
        Function<T, T> result = super.value();
        if(result == null) {
            result = Function.identity();//Functions.<T>identity();
        }

        return result;
    }
}