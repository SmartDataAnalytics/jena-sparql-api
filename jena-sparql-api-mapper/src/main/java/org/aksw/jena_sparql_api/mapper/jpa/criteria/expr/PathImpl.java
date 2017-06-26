package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class PathImpl<X>
    extends ExpressionBase<X>
    implements VPath<X>
{
    protected Path<?> parentPath;
    protected String attributeName;
    protected Class<X> valueType;

    /**
     * The attribute name by with this instance of path was reached.
     * null for the root of a path
     *
     */
    @Override
    public String getReachingAttributeName() {
        return attributeName;
    }

    public PathImpl(Path<?> parentPath, String attrName, Class<X> valueType) {
        super(valueType);
        this.parentPath = parentPath;
        this.attributeName = attrName;
        //super(parentPath, attrName, valueType);
    }

    @SuppressWarnings("unchecked")
    public <T> VExpression<T> as(Class<T> cls) {
        return (VExpression<T>)this;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public <T> T accept(PathVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public Bindable<X> getModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Path<?> getParentPath() {
        return parentPath;
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expression<Class<? extends X>> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
        return new PathImpl<Y>(this, attributeName, null);
    }

    @Override
    public String toString() {
        return (parentPath == null ? "" : parentPath.toString() + ".") + attributeName;
    }

}
