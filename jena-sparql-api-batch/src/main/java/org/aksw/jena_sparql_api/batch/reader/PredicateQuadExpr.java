package org.aksw.jena_sparql_api.batch.reader;

import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.base.Predicate;

public class PredicateQuadExpr
    implements Predicate<Quad>
{
    private Expr expr;

    public PredicateQuadExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public boolean apply(Quad quad) {
        Binding binding = QuadUtils.quadToBinding(quad);

        NodeValue tmp = ExprUtils.eval(expr, binding);

        boolean result = tmp.isBoolean() ? tmp.getBoolean() : true;
        return result;
    }
}