package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

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