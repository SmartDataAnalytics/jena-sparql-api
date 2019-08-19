package org.aksw.jena_sparql_api.concepts;

import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public interface ExprFragment {
	Expr getExpr();
	Set<Var> getVarsMentioned();
}
