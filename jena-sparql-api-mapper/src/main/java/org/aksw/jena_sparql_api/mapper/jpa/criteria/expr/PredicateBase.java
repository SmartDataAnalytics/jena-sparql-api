package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public abstract class PredicateBase
	extends org.aksw.jena_sparql_api.mapper.jpa.criteria.PredicateBase
	implements VExpression<Boolean> 
{
	protected VExpression<Boolean> expression;
	
	public PredicateBase(VExpression<Boolean> expression) {
		super();
		this.expression = expression;
	}

	@SuppressWarnings("unchecked")
	public <X> VExpression<X> as(Class<X> cls) {
		return (VExpression<X>)this; 
	}

}
