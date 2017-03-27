package org.aksw.jena_sparql_api.mapper.jpa.criteria.expr;

import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;

interface Visitable
{
	/**
	 * Get a JPQL fragment as used in WHERE clause.
	 */
	Expr asExpression(AliasContext ctx);

	/**
	 * Gets the string representation in SELECT projection.
	 */
	VarExprList asProjection(AliasContext ctx);

	/**
	 * Gets the string representation in FROM clause.
	 */
	Element asJoinable(AliasContext ctx);
}