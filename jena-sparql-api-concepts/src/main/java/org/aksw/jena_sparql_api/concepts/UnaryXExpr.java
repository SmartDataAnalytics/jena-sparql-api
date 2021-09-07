package org.aksw.jena_sparql_api.concepts;

import java.util.Map.Entry;
import java.util.Optional;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

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

    /**
     * Retuns the constant if the expression is of form
     * anyVar = const or const = ?anyVar
     *
     *
     */
    default Optional<NodeValue> tryGetConstant () {
        Expr expr = getExpr();
        Optional<NodeValue> result = Optional
                .ofNullable(ExprUtils.extractConstantConstraint(expr))
                .map(Entry::getValue);

        return result;
    }
}
