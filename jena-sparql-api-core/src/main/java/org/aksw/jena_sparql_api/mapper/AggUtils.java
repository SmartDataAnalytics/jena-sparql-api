package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class AggUtils {
    public static BindingMapper<Node> mapper(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        BindingMapper<Node> result = new BindingMapperExpr(expr);
        return result;
    }

    public static Agg<Node> literalNode(String exprStr) {
        BindingMapper<Node> m = AggUtils.mapper(exprStr);
        Agg<Node> result = new AggLiteral<Node>(m);
        return result;
    }

    public static Agg<Object> literal(String exprStr) {
        BindingMapper<Node> m = AggUtils.mapper(exprStr);
        Agg<Node> tmp = new AggLiteral<Node>(m);
        Agg<Object> result = AggTransform.create(tmp, FunctionNodeToObject.fn);
        return result;
    }

    public static Agg<Node> literalNode(Expr expr) {
        BindingMapper<Node> m = new BindingMapperExpr(expr);
        Agg<Node> result = new AggLiteral<Node>(m);
        return result;
    }

}
