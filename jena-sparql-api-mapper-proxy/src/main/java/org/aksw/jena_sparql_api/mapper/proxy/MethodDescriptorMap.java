package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;

public class MethodDescriptorMap
    extends MethodDescriptorBase
{
    protected Class<?> keyType;
    protected Class<?> valueType;
    //protected Class<?> itemType;
    //protected boolean isDynamic;

    public MethodDescriptorMap(
            Method method,
            boolean isGetter,
            boolean isFluentCompatible,
            Class<?> keyType,
            Class<?> valueType) {
        super(method, isGetter, isFluentCompatible);
        this.keyType = keyType;
        this.valueType = valueType;
        //this.isDynamic = isDynamic;
    }

//	@Override public boolean isSetter() { return false; }
    @Override public boolean isCollectionValued() { return false; }
    @Override public boolean isDynamicCollection() { return false; }

    @Override public Class<?> getType() { return null; }
    @Override public Class<?> getCollectionType() { return null; }
    @Override public Class<?> getItemType() { return null; }

    @Override public boolean isMapType() { return true; }
    @Override public Class<?> getKeyType() { return keyType; }
    @Override public Class<?> getValueType() { return valueType; }
}