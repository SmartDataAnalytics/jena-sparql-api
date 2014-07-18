package org.aksw.jena_sparql_api.geo;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class BindingMapperExpr
    implements BindingMapper<NodeValue>
{
    private Expr expr;
    
    public BindingMapperExpr(Expr expr) {
        this.expr = expr;
    }
    
    @Override
    public NodeValue map(Binding binding, long rowNum) {
        NodeValue result = ExprUtils.eval(expr, binding);
        return result;
    }
}