package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public abstract class ExpressionBase<T>
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.ExpressionBase<T>
	implements VExpression<T>
{

	public ExpressionBase(Class<? extends T> javaClass) {
		super(javaClass);
	}

	@SuppressWarnings("unchecked")
	public <X> VExpression<X> as(Class<X> cls) {
		return (VExpression<X>)this; 
	}
}
