package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;

public interface SparqlExprParser
    extends Function<String, Expr>
{
}
