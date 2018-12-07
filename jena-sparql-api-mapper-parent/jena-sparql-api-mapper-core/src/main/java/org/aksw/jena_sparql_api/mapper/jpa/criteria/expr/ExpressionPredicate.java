package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

/**
 * A no-op wrapper for treating Expression<Boolean> as a predicate.
 * Hence, the accept method delegates to the wrapped expression.
 * 
 * 
 * @author raven
 *
 */
public class ExpressionPredicate
	extends PredicateBase
{
	public ExpressionPredicate(VExpression<Boolean> expression) {
		super(expression);
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		// Delegate the call to the expression visitor
		X result = this.expression.accept(visitor);
		return result;
	}
}
