package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;


public class EqualsExpression
	extends BinaryOperatorExpression<Boolean>
{
	public EqualsExpression(VExpression<?> a, VExpression<?> b) {
		super(Boolean.class, a, b);
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}
