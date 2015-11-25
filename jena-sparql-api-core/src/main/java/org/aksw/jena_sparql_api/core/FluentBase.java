package org.aksw.jena_sparql_api.core;

import com.google.common.base.Supplier;

public class FluentBase<T, P>
    implements ParentSuppliable<P>
{
    protected Supplier<P> parentSupplier;
    protected T fn;

    public FluentBase() {
        this(null);
    }

    public FluentBase(T fn) {
        this(fn, null);
    }

    public FluentBase(T fn, Supplier<P> parentSupplier) {
        super();
        this.fn = fn;
        this.parentSupplier = parentSupplier;
    }

    /*
    void enterFn(final FluentFnBase<T, P> subFluent) {
        final FluentBase<T, P> self = this;

        //final FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<T>> result = new FluentQueryExecutionFactoryFn<FluentQueryExecutionFactory<T>>();
        subFluent.setParentSupplier(new Supplier<P>() {
                @Override
                public P get() {
                    // Apply the collection transformations
                    Function<T, T> transform = subFluent.create();
                    fn = transform.apply(fn);

                    return self;
                }
            });

        return result;
    }
    */

    @Override
    public void setParentSupplier(Supplier<P> parentSupplier) {
        this.parentSupplier = parentSupplier;
    }

    /**
     * Create the result value.
     *
     * This method should *never* be used directly by client code - instead, use create().
     * However, it may be useful for debugging.
     *
     *
     * @return
     */
    public T value() {
        return fn;
    }

    public T create() {
        if(parentSupplier != null) {
            throw new RuntimeException("Calling .create() is invalid here. You probably intended to call .end()");
        }

        T result = value();

        return result;
    }

    public P end() {
        P result;
        if(parentSupplier == null) {
            throw new RuntimeException("Calling .end() is invalid here. You probably intended to call .create()");
        } else {
            result = parentSupplier.get();
        }

        return result;
    }
}
