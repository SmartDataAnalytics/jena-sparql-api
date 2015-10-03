package org.aksw.jena_sparql_api.batch.reader;

import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.base.Predicate;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;

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

        NodeValue tmp = expr.eval(binding, FunctionEnvBase.createTest());

        boolean result = tmp.isBoolean() ? tmp.getBoolean() : true;
        return result;
    }
}