package org.aksw.jena_sparql_api.concepts;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterables;

public class UnaryXExprImpl
    implements UnaryXExpr
{
    protected Expr expr;
    protected Var var;

    protected Set<Var> varsMentioned;

    public UnaryXExprImpl(Expr expr, Var var) {
        this.expr = expr;
        this.var = var;

        this.varsMentioned = Collections.singleton(var);
    }


    @Override
    public Set<Var> getVarsMentioned() {
        return varsMentioned;
    }

    @Override
    public Var getVar() {
        return var;
    }

    @Override
    public Expr getExpr() {
        return expr;
    }


    public static UnaryXExpr create(Expr expr) {
        Set<Var> vars = ExprVars.getVarsMentioned(expr);
        if(vars.size() > 1) {
            throw new RuntimeException("too many variables in expr - got " + vars);
        }

        Var var = Iterables.getFirst(vars, null);

        UnaryXExpr result = new UnaryXExprImpl(expr, var);
        return result;
    }

    public static NodeValue eval(UnaryXExpr xexpr, Node value) {
        Var var = xexpr.getVar();
        Expr expr = xexpr.getExpr();

        Binding b = BindingFactory.binding(var, value);
        NodeValue result = ExprUtils.eval(expr, b);
        return result;

    }

}
