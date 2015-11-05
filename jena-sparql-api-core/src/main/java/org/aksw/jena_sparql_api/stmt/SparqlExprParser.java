package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.expr.Expr;

public interface SparqlExprParser
    extends Function<String, Expr>
{
}
