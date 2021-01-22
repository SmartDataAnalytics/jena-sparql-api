package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.enhanced.Implementation;

/**
 * Implementation that defers creation of a delegate to the first
 * access to its methods. This can greatly increase application
 * startup times when the initialization of the actual implementation
 * performs reflection and byte code fiddling using cglib (weaving).
 * 
 * @author raven
 *
 */
public class ImplementationLazy
    extends ImplementationDelegate
{
    protected Supplier<Implementation> ctor;
    protected Class<?> targetClass;
    protected Implementation delegate;

    /**
     *
     * @param ctor The supplier from which the delagate of this class is obtained
     * @param targetClass The class the implementation is for. Serves informational purpose only.
     */
    public ImplementationLazy(Supplier<Implementation> ctor, Class<?> targetClass) {
        super();
        this.ctor = ctor;
        this.targetClass = targetClass;
        this.delegate = null;
    }

    @Override
    protected Implementation getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = Objects.requireNonNull(ctor.get(),
                            "Lazy request for implementation for " + targetClass + " was answered with null");
                }
            }
        }

        return delegate;
    }

}
