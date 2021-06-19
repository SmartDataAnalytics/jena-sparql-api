package org.aksw.jena_sparql_api.utils;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class ExprListUtils {
    public static boolean contains(ExprList exprList, Expr expr) {
        boolean result = false;

        for(Expr item : exprList) {
            result = item.equals(expr);
            if(result) {
                break;
            }
        }

        return result;
    }

    public static ExprList fromUris(Iterable<String> uris) {
        List<Node> nodes = NodeUtils.fromUris(uris);
        ExprList result = nodesToExprs(nodes);
        return result;
    }

    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr e = ExprUtils.nodeToExpr(node);
            result.add(e);
        }

        return result;
    }

    /**
     * Similar to {@link #evalBoolean(ExprList, Binding)} with the difference
     * that if any evaluation raises an exception the overall result is 'false'.
     * Hence, this method does not throw exceptions.
     *
     * @param el
     * @param binding
     * @return
     */
    public static boolean evalEffectiveBoolean(ExprList el, Binding binding) {
        boolean result;
        try {
            result = evalBoolean(el, binding);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    /**
     * Evaluate an ExprList as a logical conjunction.
     * If the list is empty then the result is true.
     * No exceptions are caught.
     *
     * @param el
     * @param binding
     * @return
     */
    public static boolean evalBoolean(ExprList el, Binding binding) {
        Context context = ARQ.getContext().copy();
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        FunctionEnv env = new ExecutionContext(context, null, null, null);

        NodeValue r = NodeValue.TRUE;
        for (Expr expr : el) {
            r = new E_LogicalAnd(expr, r).eval(binding, env);
            if (NodeValue.TRUE.equals(r)) {
                break;
            }
        }

        boolean result = r.getBoolean();
        return result;
    }
}
