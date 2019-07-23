package org.aksw.jena_sparql_api.concepts;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public interface UnaryXExpr
	extends XExpr
{
	Var getVar();
	Expr getExpr();
}
