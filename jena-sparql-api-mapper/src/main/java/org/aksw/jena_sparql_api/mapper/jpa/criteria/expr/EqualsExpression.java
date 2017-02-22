package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;


public class EqualsExpression
	extends BinaryOperatorExpression<Boolean>
{
	public EqualsExpression(Expression<?> a, Expression<?> b) {
		super(Boolean.class, a, b);
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}
