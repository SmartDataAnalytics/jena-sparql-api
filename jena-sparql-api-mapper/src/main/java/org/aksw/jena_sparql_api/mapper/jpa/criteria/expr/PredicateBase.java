package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public class PredicateBase
	extends ExpressionBase<Boolean>
	implements Predicate
	//implements VExpression<Boolean> 
{
	protected VExpression<Boolean> expression;
	
	public PredicateBase(VExpression<Boolean> expression) {
		super(Boolean.class);
		this.expression = expression;
	}

	@SuppressWarnings("unchecked")
	public <X> VExpression<X> as(Class<X> cls) {
		return (VExpression<X>)this; 
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanOperator getOperator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNegated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate not() {
		return new ExpressionPredicate(new LogicalNotExpression(this));
	}

}
