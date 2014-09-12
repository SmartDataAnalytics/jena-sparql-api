package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class BindingMapperExpr
    implements BindingMapper<Node> {

    private Expr expr;

    public BindingMapperExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public Node map(Binding binding, long rowNum) {
        NodeValue nv = ExprUtils.eval(expr, binding);
        Node result = nv.asNode();
        return result;
    }
}