package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public abstract class ExpressionBase<T>
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.ExpressionBase<T>
	implements Expression<T>
{

	public ExpressionBase(Class<T> javaClass) {
		super(javaClass);
	}

	@SuppressWarnings("unchecked")
	public <X> Expression<X> as(Class<X> cls) {
		return (Expression<X>)this; 
	}
}
