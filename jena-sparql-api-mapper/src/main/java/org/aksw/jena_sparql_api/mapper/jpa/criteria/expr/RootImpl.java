package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

public class RootImpl<X>
    extends FromImpl<X, X>
    implements Root<X>
{
    protected Class<X> javaType;
    protected EntityType<X> entityType;

    public RootImpl(Class<X> javaType) {
        this(javaType, null);
    }

    public RootImpl(EntityType<X> entityType) {
        this(entityType.getJavaType(), entityType);
    }


    public RootImpl(Class<X> root, EntityType<X> entityType) {
        super(null, null, root);
        this.javaType = root;
        this.entityType = entityType;
    }

    @Override
    public Class<? extends X> getJavaType() {
        return javaType;
    }

    @Override
    public EntityType<X> getModel() {
        return entityType;
    }

    @Override
    public Path<?> getParentPath() {
        return null;
    }


    @Override
    public String toString() {
        return "RootImpl [javaType=" + javaType + ", entityType=" + entityType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
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
        RootImpl other = (RootImpl) obj;
        if (entityType == null) {
            if (other.entityType != null)
                return false;
        } else if (!entityType.equals(other.entityType))
            return false;
        if (javaType == null) {
            if (other.javaType != null)
                return false;
        } else if (!javaType.equals(other.javaType))
            return false;
        return true;
    }

    @Override
    public String getReachingAttributeName() {
        return null;
    }

    @Override
    public <T> T accept(PathVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
