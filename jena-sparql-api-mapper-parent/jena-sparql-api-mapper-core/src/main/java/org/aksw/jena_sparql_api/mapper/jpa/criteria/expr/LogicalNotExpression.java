package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public class LogicalNotExpression
	extends UnaryOperatorExpression<Boolean>
{
	public LogicalNotExpression(VExpression<Boolean> expression) {
		super(Boolean.class, expression);
	}

//	@Override
//	public boolean isNegated() {
//		return true;
//	}
	
	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}
