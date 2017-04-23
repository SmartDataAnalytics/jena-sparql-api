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
        super();
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
}
