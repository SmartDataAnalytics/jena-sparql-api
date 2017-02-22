package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.CriteriaEnv;

public class PathImpl<X>
	extends ExpressionBase<X>
	implements Path<X>
{
	protected CriteriaEnv env;

	protected Path<?> parentPath;
	protected String attributeName;
	protected Class<X> valueType;

	public String getAttributeName() {
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
	public Bindable<X> getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path<?> getParentPath() {
		// TODO Auto-generated method stub
		return null;
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
}
