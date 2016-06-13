package org.aksw.jena_sparql_api.concept.builder.utils;

import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Collection of utils for creating expressions
 *
 * @author raven
 *
 */
public class Exprs {
    public static Expr langMatches(String arg) {
        ExprVar ev = new ExprVar("s");
        return new E_LangMatches(new E_Lang(ev), NodeValue.makeString(arg));
    }
}
