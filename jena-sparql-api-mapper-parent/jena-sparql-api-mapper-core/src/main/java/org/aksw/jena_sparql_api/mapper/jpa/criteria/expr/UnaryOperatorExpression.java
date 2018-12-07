package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

public abstract class UnaryOperatorExpression<T>
	extends ExpressionBase<T>
{
	protected VExpression<?> operand; 
	
	public UnaryOperatorExpression(Class<? extends T> javaClass, VExpression<?> operand) {
		super(javaClass);
		this.operand = operand;
	}
	
	public VExpression<?> getOperand() {
		return operand;
	}
}
