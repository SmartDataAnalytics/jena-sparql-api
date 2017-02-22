package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;


import org.aksw.jena_sparql_api.mapper.jpa.criteria.ExpressionBase;

public abstract class BinaryOperatorExpression<T>
	extends ExpressionBase<T>
{
	protected Expression<?> leftHandOperand; 
	protected Expression<?> rightHandOperand;

	public BinaryOperatorExpression(Class<T> javaClass, Expression<?> leftHandOperand, Expression<?> rightHandOperand) {
		super(javaClass);
		this.leftHandOperand = leftHandOperand;
		this.rightHandOperand = rightHandOperand;
	}

	public Expression<?> getLeftHandOperand() {
		return leftHandOperand;
	}

	public Expression<?> getRightHandOperand() {
		return rightHandOperand;
	}

}
