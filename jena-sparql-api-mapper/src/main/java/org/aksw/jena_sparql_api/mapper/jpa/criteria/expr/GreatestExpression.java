package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public class GreatestExpression<T>
	extends UnaryOperatorExpression<T>
{
	public GreatestExpression(Class<T> javaClass, VExpression<T> operand) {
		super(javaClass, operand);
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}

}
