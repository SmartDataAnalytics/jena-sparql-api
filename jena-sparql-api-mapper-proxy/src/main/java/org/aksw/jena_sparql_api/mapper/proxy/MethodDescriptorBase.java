package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;

public abstract class MethodDescriptorBase
    implements MethodDescriptor
{
    protected Method method;
    protected boolean isGetter;
    protected boolean isFluentCompatible;

    public MethodDescriptorBase(Method method, boolean isGetter, boolean isFluentCompatible) {
        this.method = method;
        this.isGetter = isGetter;
        this.isFluentCompatible = isFluentCompatible;
    }

    @Override public Method getMethod() { return method; }
    @Override public boolean isGetter() { return isGetter; }
    @Override public boolean isFluentCompatible() { return isFluentCompatible; } //throw new RuntimeException("not applicable"); }
}