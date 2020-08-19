package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

public class BindingMapperExpr
    implements BindingMapperVarAware<Node> {

    private Expr expr;

    public BindingMapperExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Node apply(Binding binding, Long rowNum) {
        NodeValue nv = ExprUtils.eval(expr, binding);
        Node result = nv.asNode();
        return result;
    }

    @Override
    public Set<Var> getVarsMentioned() {
        Set<Var> result = ExprVars.getVarsMentioned(expr);
        return result;
    }
}