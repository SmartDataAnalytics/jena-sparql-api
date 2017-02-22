package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Expression;

import org.aksw.jena_sparql_api.mapper.jpa.criteria.PredicateBase;

public class LogicalNotPredicate
	extends PredicateBase
{
	public LogicalNotPredicate(Expression<Boolean> expression) {
		super();
	}

	@Override
	public <X> X accept(ExpressionVisitor<X> visitor) {
		X result = visitor.visit(this);
		return result;
	}
}
