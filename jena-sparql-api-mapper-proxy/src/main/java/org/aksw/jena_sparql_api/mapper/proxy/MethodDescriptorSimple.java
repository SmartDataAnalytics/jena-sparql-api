package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;

public class MethodDescriptorSimple
    extends MethodDescriptorBase
{
    protected Class<?> type;
    // TODO Add support for wrapping the type in a java.unit.Optional
    // Should we treat Optional as a (cardinality restricted) collection?

    public MethodDescriptorSimple(Method method, boolean isGetter, boolean isFluentCompatible, Class<?> type) {
        super(method, isGetter, isFluentCompatible);
        this.type = type;
        this.isFluentCompatible = isFluentCompatible;
    }

    @Override public boolean isCollectionValued() { return false; }
    @Override public boolean isDynamicCollection() { return false; }
    @Override public Class<?> getType() { return type; }

    @Override public Class<?> getCollectionType() { return null; }
    @Override public Class<?> getItemType() { return null; }

    @Override public boolean isMapType() { return false; }
    @Override public Class<?> getKeyType() { return null; }
    @Override public Class<?> getValueType() { return null; }
}