package org.aksw.jena_sparql_api.schema_mapping;

import org.apache.jena.sparql.expr.Expr;

public interface ExprRewrite {
	Expr rewrite(Expr arg);
}
