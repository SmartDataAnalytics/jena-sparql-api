package org.aksw.jena_sparql_api.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class ExprListUtils {
    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr e = ExprUtils.nodeToExpr(node);
            result.add(e);
        }

        return result;
    }
}
