package org.aksw.jena_sparql_api.mapper.proxy;

import java.lang.reflect.Method;

public class MethodDescriptorCollection
    extends MethodDescriptorBase
{
    protected Class<?> collectionType;
    protected Class<?> itemType;
    protected boolean isDynamic;

    public MethodDescriptorCollection(Method method, boolean isGetter, boolean isFluentCompatible, Class<?> collectionType, Class<?> itemType, boolean isDynamic) {
        super(method, isGetter, isFluentCompatible);
        this.collectionType = collectionType;
        this.itemType = itemType;
        this.isDynamic = isDynamic;
    }

//	@Override public boolean isSetter() { return false; }
    @Override public boolean isCollectionValued() { return true; }
    @Override public boolean isDynamicCollection() { return isDynamic; }

    @Override public Class<?> getType() { return null; }
    @Override public Class<?> getCollectionType() { return collectionType; }
    @Override public Class<?> getItemType() { return itemType; }

    @Override public boolean isMapType() { return false; }
    @Override public Class<?> getKeyType() { return null; }
    @Override public Class<?> getValueType() { return null; }
}