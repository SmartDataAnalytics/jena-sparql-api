package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public class LogicalNotPredicate
	extends PredicateBase
{
	public LogicalNotPredicate(VExpression<Boolean> expression) {
		super(expression);
	}

	@Override
	public boolean isNegated() {
		return true;
	}
	
	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}
