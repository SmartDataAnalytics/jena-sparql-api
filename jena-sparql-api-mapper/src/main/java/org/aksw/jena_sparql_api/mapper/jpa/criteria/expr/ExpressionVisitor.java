package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import javax.persistence.criteria.Path;

public interface ExpressionVisitor<T> {
	T visit(Path<?> e);

	T visit(LogicalNotPredicate e);
	
	T visit(EqualsExpression e);
	
	T visit(ValueExpression<?> e);
}
