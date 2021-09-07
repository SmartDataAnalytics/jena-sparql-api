package org.aksw.jena_sparql_api.concepts;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;

public interface UnaryXExpr
    extends XExpr
{
    Var getVar();
    Expr getExpr();


    /**
     * Whether the expression is of the form ?x = ?x
     * (Unary expressions must always have a variable so 'true' itself cannot be used)
     */
    default boolean isAlwaysTrue() {
        Var v = getVar();
        ExprVar ev = new ExprVar(v);
        Expr reference = new E_Equals(ev, ev);

        Expr expr = getExpr();

        boolean result = expr.equals(reference);
        return result;
    }
}
