package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

public class AggUtils {
    public static <T> T accumulate(Agg<T> agg, ResultSet rs) {
        Acc<T> acc = agg.createAccumulator();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            acc.accumulate(binding);
        }

        T result = acc.getValue();
        return result;
    }

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
