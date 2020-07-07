package org.aksw.jena_sparql_api.mapper.descriptor.impl;

import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;

public class TypeCollectionImpl
    extends TypeBase
    implements TypeCollection
{
    protected Class<?> collectionClass;
    protected SimpleType itemType;

    public TypeCollectionImpl(Class<?> collectionClass, SimpleType itemType) {
        super();
        this.collectionClass = collectionClass;
        this.itemType = itemType;
    }

    @Override
    public Class<?> getCollectionClass() {
        return collectionClass;
    }

    @Override
    public SimpleType getItemType() {
        return itemType;
    }

    @Override
    public SimpleType stricterType(SimpleType other) {
        SimpleType result = null;
        if(other.isCollection()) {
            TypeCollection o = other.asCollection();
            Class<?> effectiveCollectionClass = MapperProxyUtils.getStricterType(collectionClass, o.getCollectionClass());

            SimpleType effectiveItemType = itemType.stricterType(o.getItemType());
            //Class<?> effectiveitemType = MapperProxyUtils.getStricterType(itemType, o.getItemType());
            result = effectiveCollectionClass == null || effectiveItemType == null
                    ? null
                    : new TypeCollectionImpl(effectiveCollectionClass, effectiveItemType);
        }
        return result;
    }

    @Override
    public boolean isList() {
        boolean result = List.class.isAssignableFrom(collectionClass);
        return result;
    }

    @Override
    public boolean isSet() {
        boolean result = Set.class.isAssignableFrom(collectionClass);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((collectionClass == null) ? 0 : collectionClass.hashCode());
        result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeCollectionImpl other = (TypeCollectionImpl) obj;
        if (collectionClass == null) {
            if (other.collectionClass != null)
                return false;
        } else if (!collectionClass.equals(other.collectionClass))
            return false;
        if (itemType == null) {
            if (other.itemType != null)
                return false;
        } else if (!itemType.equals(other.itemType))
            return false;
        return true;
    }
}
