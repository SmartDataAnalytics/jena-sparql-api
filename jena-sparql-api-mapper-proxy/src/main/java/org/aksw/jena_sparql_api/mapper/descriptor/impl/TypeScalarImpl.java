package org.aksw.jena_sparql_api.mapper.descriptor.impl;

import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;

public class TypeScalarImpl
    extends TypeBase
    implements TypeScalar
{
    protected Class<?> itemClass;

    public TypeScalarImpl(Class<?> itemClass) {
        super();
        this.itemClass = itemClass;
    }

    public Class<?> getItemClass() {
        return itemClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((itemClass == null) ? 0 : itemClass.hashCode());
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
        TypeScalarImpl other = (TypeScalarImpl) obj;
        if (itemClass == null) {
            if (other.itemClass != null)
                return false;
        } else if (!itemClass.equals(other.itemClass))
            return false;
        return true;
    }

    @Override
    public SimpleType stricterType(SimpleType other) {
        SimpleType result = null;
        if(other.isScalar()) {
            TypeScalar o = other.asScalar();
            Class<?> otherClass = o.getItemClass();
            Class<?> effectiveType = MapperProxyUtils.getStricterType(itemClass, otherClass);
            result = effectiveType == null ? null : new TypeScalarImpl(effectiveType);
        }
        return result;
    }
}
