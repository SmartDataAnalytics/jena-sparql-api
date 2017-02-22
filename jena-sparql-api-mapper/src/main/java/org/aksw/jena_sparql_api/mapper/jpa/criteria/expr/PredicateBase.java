package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public abstract class PredicateBase
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.PredicateBase
	implements Expression<Boolean> 
{
	protected Expression<Boolean> expression;
	
	public PredicateBase(Expression<Boolean> expression) {
		super();
		this.expression = expression;
	}

	@SuppressWarnings("unchecked")
	public <X> Expression<X> as(Class<X> cls) {
		return (Expression<X>)this; 
	}
}
