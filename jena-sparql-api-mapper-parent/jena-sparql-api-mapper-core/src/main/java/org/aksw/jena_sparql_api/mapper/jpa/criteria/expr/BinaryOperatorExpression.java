package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import java.util.Objects;

public abstract class BinaryOperatorExpression<T>
	extends ExpressionBase<T>
{
	protected VExpression<?> leftHandOperand; 
	protected VExpression<?> rightHandOperand;

	public BinaryOperatorExpression(Class<T> javaClass, VExpression<?> leftHandOperand, VExpression<?> rightHandOperand) {
		super(javaClass);
		
		Objects.requireNonNull(leftHandOperand);
		Objects.requireNonNull(rightHandOperand);
		
		this.leftHandOperand = leftHandOperand;
		this.rightHandOperand = rightHandOperand;
	}

	public VExpression<?> getLeftHandOperand() {
		return leftHandOperand;
	}

	public VExpression<?> getRightHandOperand() {
		return rightHandOperand;
	}

}
